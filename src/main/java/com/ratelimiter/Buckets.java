package com.ratelimiter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// The token buckets in Redis. All the bucket maths is in lua/take_token.lua; this class just calls it.
@Service
public class Buckets {

    private static final Logger log = LoggerFactory.getLogger(Buckets.class);

    // remaining is null when Redis couldn't be reached.
    public record Result(boolean allowed, Long remaining, long retryAfterMs, boolean redisDown) {
    }

    private final StringRedisTemplate redis;
    private final boolean failOpen;
    @SuppressWarnings("rawtypes")
    private final DefaultRedisScript<List> script = new DefaultRedisScript<>();

    public Buckets(StringRedisTemplate redis, @Value("${ratelimiter.fail-open:true}") boolean failOpen) {
        this.redis = redis;
        this.failOpen = failOpen;
        script.setLocation(new ClassPathResource("lua/take_token.lua"));
        script.setResultType(List.class);
    }

    public static String key(UUID tenantId, UUID ruleId, String key) {
        return "rl:" + tenantId + ":" + ruleId + ":" + key;
    }

    // If Redis is down (or the breaker is open because it recently was), the fallback below answers instead.
    @CircuitBreaker(name = "redis", fallbackMethod = "redisDown")
    public Result check(String redisKey, int capacity, int windowSeconds) {
        List<?> out = redis.execute(script, List.of(redisKey), String.valueOf(capacity), String.valueOf(windowSeconds));
        boolean allowed = ((Long) out.get(0)) == 1;
        double tokens = Double.parseDouble((String) out.get(1));
        long retryAfterMs = allowed ? 0 : (long) Math.ceil((1 - tokens) * windowSeconds * 1000.0 / capacity);
        return new Result(allowed, (long) Math.floor(tokens), retryAfterMs, false);
    }

    // Fail open: let traffic through while Redis is down. Fail closed: deny everything. Set in application.yml.
    Result redisDown(String redisKey, int capacity, int windowSeconds, Throwable error) {
        log.warn("redis check failed, failing {}: {}", failOpen ? "open" : "closed", error.toString());
        return new Result(failOpen, null, 0, true);
    }

    public void reset(String redisKey) {
        redis.delete(redisKey);
    }
}
