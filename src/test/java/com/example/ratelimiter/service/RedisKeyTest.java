// GENERATED TESTS
package com.example.ratelimiter.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RedisKeyTest {

  @Test
  void redis_key_generated_correctly() throws IOException {
    RedisLuaScriptService svc = new RedisLuaScriptService(mock(StringRedisTemplate.class));
    UUID tenant = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID rule = UUID.fromString("22222222-2222-2222-2222-222222222222");
    String key = svc.buildKey(tenant, rule);
    assertEquals("rate_limit:11111111-1111-1111-1111-111111111111:22222222-2222-2222-2222-222222222222", key);
  }
}
