package com.example.ratelimiter.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.io.IOException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RedisKeyTest {
    @Test
    void redis_key_has_key_value() throws IOException {
        RedisLuaScriptService svc = new RedisLuaScriptService(mock(StringRedisTemplate.class));
        UUID tenant = UUID.randomUUID();
        UUID rule = UUID.randomUUID();
        String key = svc.buildKey(tenant, rule, "user-1");
        assertTrue(key.endsWith(":user-1"));
    }
}
