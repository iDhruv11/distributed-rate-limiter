package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.CheckLimitResponse;
import com.example.ratelimiter.dto.ConcurrencyResult;
import com.example.ratelimiter.dto.QuotaState;
import com.example.ratelimiter.entity.RateLimitRule;
import com.example.ratelimiter.exception.RedisUnavailableException;
import com.example.ratelimiter.exception.RuleNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class RateLimiterService {

    private final RateLimitRuleService ruleService;
    private final RedisLuaScriptService redisLua;
    private final TokenBucketAlgorithm tokenBucket;
    private final String failMode;

    public RateLimiterService(RateLimitRuleService ruleService,
                              RedisLuaScriptService redisLua,
                              TokenBucketAlgorithm tokenBucket,
                              @Value("${ratelimiter.redis.fail-mode:open}") String failMode) {
        this.ruleService = ruleService;
        this.redisLua = redisLua;
        this.tokenBucket = tokenBucket;
        this.failMode = failMode;
    }

    public CheckLimitResponse checkLimit(UUID tenantId, UUID ruleId) {
        RateLimitRule rule = ruleService.getRuleById(tenantId, ruleId);
        if (!Boolean.TRUE.equals(rule.getEnabled())) {
            throw new RuleNotFoundException("rule disabled or not found");
        }

        String redisKey = redisLua.buildKey(tenantId, ruleId);
        long now = System.currentTimeMillis() / 1000;

        try {
            QuotaState state = redisLua.executeCheckAndDecrement(redisKey, rule, now);
            return buildResponse(state, rule, now, true);
        } catch (RedisUnavailableException ex) {
            return handleRedisFailure(rule, now, ex);
        }
    }

    public CheckLimitResponse getQuotaRemaining(UUID tenantId, UUID ruleId) {
        String redisKey = redisLua.buildKey(tenantId, ruleId);
        long now = System.currentTimeMillis() / 1000;

        try {
            QuotaState state = redisLua.readQuota(redisKey);
            CheckLimitResponse resp = new CheckLimitResponse();

            if (state == null) {
                resp.setInitialized(false);
                resp.setAllowed(false);
                return resp;
            }

            double tokens = tokenBucket.refillFromState(state, now);
            resp.setInitialized(true);
            resp.setAllowed(tokenBucket.hasTokens(tokens));
            resp.setRemainingQuota((long) Math.floor(tokens));
            resp.setRefillRate(state.getRefillRate());
            resp.setWindowSeconds(state.getWindowSizeSeconds());
            resp.setResetAt(tokenBucket.estimateFullAt(tokens, state.getCapacity(), state.getRefillRate(), now));
            return resp;
        } catch (RedisUnavailableException ex) {
            throw ex;
        }
    }

    public void resetQuota(UUID tenantId, UUID ruleId) {
        ruleService.getRuleById(tenantId, ruleId);
        String redisKey = redisLua.buildKey(tenantId, ruleId);
        try {
            redisLua.deleteKey(redisKey);
        } catch (RedisUnavailableException ex) {
            if ("closed".equalsIgnoreCase(failMode)) {
                throw ex;
            }
        }
    }

    public ConcurrencyResult runConcurrencyDemo(UUID tenantId, UUID ruleId) {
        ruleService.getRuleById(tenantId, ruleId);
        redisLua.deleteKey(redisLua.buildKey(tenantId, ruleId));

        long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(100);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            futures.add(pool.submit(() -> checkLimit(tenantId, ruleId).isAllowed()));
        }

        int allowed = 0;
        int denied = 0;
        for (Future<Boolean> f : futures) {
            try {
                if (f.get()) {
                    allowed++;
                } else {
                    denied++;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();

        ConcurrencyResult result = new ConcurrencyResult();
        result.setAllowed(allowed);
        result.setDenied(denied);
        result.setTookMs(System.currentTimeMillis() - start);
        return result;
    }

    private CheckLimitResponse buildResponse(QuotaState state, RateLimitRule rule, long now, boolean initialized) {
        CheckLimitResponse resp = new CheckLimitResponse();
        resp.setInitialized(initialized);
        resp.setWindowSeconds(state.getWindowSizeSeconds() != null ? state.getWindowSizeSeconds() : rule.getWindowSizeSeconds());
        resp.setRemainingQuota((long) Math.floor(state.getTokens()));
        resp.setRefillRate(state.getRefillRate());

        int cap = state.getCapacity() != null ? state.getCapacity() : rule.getLimitValue();
        double rate = state.getRefillRate() != null ? state.getRefillRate() : tokenBucket.getRefillRate(rule);
        resp.setResetAt(tokenBucket.estimateFullAt(state.getTokens(), cap, rate, now));

        if (state.getAllowed() == 1) {
            resp.setAllowed(true);
        } else {
            resp.setAllowed(false);
            resp.setReason("limit_exceeded");
            resp.setRetryAfterSeconds(tokenBucket.estimateRetryAfter(state.getTokens(), rate));
        }
        return resp;
    }

    private CheckLimitResponse handleRedisFailure(RateLimitRule rule, long now, RedisUnavailableException ex) {
        if ("closed".equalsIgnoreCase(failMode)) {
            throw ex;
        }
        CheckLimitResponse resp = new CheckLimitResponse();
        resp.setAllowed(true);
        resp.setRemainingQuota(rule.getLimitValue().longValue());
        resp.setRefillRate(tokenBucket.getRefillRate(rule));
        resp.setResetAt(now);
        resp.setWindowSeconds(rule.getWindowSizeSeconds());
        resp.setInitialized(false);
        return resp;
    }
}
