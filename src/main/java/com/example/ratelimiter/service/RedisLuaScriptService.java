package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.QuotaState;
import com.example.ratelimiter.entity.RateLimitRule;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper mapper = new ObjectMapper();

    public RedisLuaScriptService(StringRedisTemplate redis) throws IOException {
        this.redis = redis;
        this.checkScript = loadScript("lua/check_and_decrement.lua");
    }

    private DefaultRedisScript<String> loadScript(String path) throws IOException {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setResultType(String.class);
        script.setScriptText(new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8));
        return script;
    }

    public String buildKey(UUID tenantId, UUID ruleId, String keyValue) {
        return "rate_limit:" + tenantId + ":" + ruleId + ":" + keyValue;
    }

    public QuotaState executeCheckAndDecrement(String redisKey, RateLimitRule rule, long now) throws Exception {
        List<String> keys = Collections.singletonList(redisKey);
        String raw = redis.execute(checkScript, keys,
                String.valueOf(rule.getLimitValue()),
                String.valueOf(rule.getWindowSizeSeconds()),
                String.valueOf(now));
        return mapper.readValue(raw, QuotaState.class);
    }
}
