package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.QuotaState;
import com.example.ratelimiter.entity.RateLimitRule;
import com.example.ratelimiter.exception.RedisUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RedisLuaScriptService {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<String> checkScript;
    private final DefaultRedisScript<String> initScript;
    private final ObjectMapper mapper = new ObjectMapper();

    public RedisLuaScriptService(StringRedisTemplate redis) throws IOException {
        this.redis = redis;
        this.checkScript = loadScript("lua/check_and_decrement.lua");
        this.initScript = loadScript("lua/init_state.lua");
    }

    private DefaultRedisScript<String> loadScript(String path) throws IOException {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setResultType(String.class);
        script.setScriptText(new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8));
        return script;
    }

    public String buildKey(UUID tenantId, UUID ruleId) {
        return "rate_limit:" + tenantId + ":" + ruleId;
    }
    
    public boolean ping() {
        try {
            String pong = redis.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasState(String redisKey) {
        return Boolean.TRUE.equals(redis.hasKey(redisKey));
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "checkFallback")
    public QuotaState executeCheckAndDecrement(String redisKey, RateLimitRule rule, long now) {
        try {
            List<String> keys = Collections.singletonList(redisKey);
            String raw;
            if (hasState(redisKey)) {
                raw = redis.execute(checkScript, keys, String.valueOf(now));
            } else {
                double refillRate = (double) rule.getLimitValue() / rule.getWindowSizeSeconds();
                raw = redis.execute(checkScript, keys,
                        String.valueOf(now),
                        String.valueOf(rule.getLimitValue()),
                        String.valueOf(refillRate),
                        String.valueOf(rule.getWindowSizeSeconds()),
                        rule.getKeyType().name(),
                        rule.getKeyValue());
            }
            if (raw == null) {
                throw new RedisUnavailableException("redis returned null");
            }
            return mapper.readValue(raw, QuotaState.class);
        } catch (RedisUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisUnavailableException("redis script failed", e);
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "initFallback")
    public QuotaState initFullState(String redisKey, RateLimitRule rule, long now) {
        try {
            double refillRate = (double) rule.getLimitValue() / rule.getWindowSizeSeconds();
            List<String> keys = Collections.singletonList(redisKey);
            String raw = redis.execute(initScript, keys,
                    String.valueOf(rule.getLimitValue()),
                    String.valueOf(refillRate),
                    String.valueOf(rule.getWindowSizeSeconds()),
                    rule.getKeyType().name(),
                    rule.getKeyValue(),
                    String.valueOf(now));
            return mapper.readValue(raw, QuotaState.class);
        } catch (Exception e) {
            throw new RedisUnavailableException("redis init failed", e);
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "readFallback")
    public QuotaState readQuota(String redisKey) {
        try {
            String raw = redis.opsForValue().get(redisKey);
            if (raw == null) {
                return null;
            }
            return mapper.readValue(raw, QuotaState.class);
        } catch (Exception e) {
            throw new RedisUnavailableException("redis read failed", e);
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "deleteFallback")
    public void deleteKey(String redisKey) {
        try {
            redis.delete(redisKey);
        } catch (Exception e) {
            throw new RedisUnavailableException("redis delete failed", e);
        }
    }

    QuotaState checkFallback(String redisKey, RateLimitRule rule, long now, Throwable t) {
        throw new RedisUnavailableException("circuit open or redis down", t);
    }

    QuotaState initFallback(String redisKey, RateLimitRule rule, long now, Throwable t) {
        throw new RedisUnavailableException("circuit open or redis down", t);
    }

    QuotaState readFallback(String redisKey, Throwable t) {
        throw new RedisUnavailableException("circuit open or redis down", t);
    }

    void deleteFallback(String redisKey, Throwable t) {
        throw new RedisUnavailableException("circuit open or redis down", t);
    }
}
