package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.CheckLimitRequest;
import com.example.ratelimiter.dto.CheckLimitResponse;
import com.example.ratelimiter.dto.QuotaState;
import com.example.ratelimiter.entity.RateLimitRule;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RateLimiterService {
    private final RateLimitRuleService ruleService;
    private final RedisLuaScriptService redisLua;

    public RateLimiterService(RateLimitRuleService ruleService, RedisLuaScriptService redisLua) {
        this.ruleService = ruleService;
        this.redisLua = redisLua;
    }

    public CheckLimitResponse checkLimit(UUID tenantId, CheckLimitRequest req) {
        System.out.println("check limit for " + req.getRuleId());
        RateLimitRule rule = ruleService.getRuleById(req.getRuleId());
        String redisKey = redisLua.buildKey(tenantId, req.getRuleId(), req.getKeyValue());
        long now = System.currentTimeMillis() / 1000;
        try {
            QuotaState state = redisLua.executeCheckAndDecrement(redisKey, rule, now);
            CheckLimitResponse resp = new CheckLimitResponse();
            resp.setAllowed(state.getAllowed() == 1);
            resp.setRemainingQuota((long) state.getCurrentQuota());
            resp.setResetAt(state.getResetAt());
            resp.setWindowSeconds(rule.getWindowSizeSeconds());
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
