// GENERATED TESTS

package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.QuotaState;
import com.example.ratelimiter.entity.KeyType;
import com.example.ratelimiter.entity.RateLimitRule;
import com.example.ratelimiter.entity.WindowType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketAlgorithmTest {

  private final TokenBucketAlgorithm algo = new TokenBucketAlgorithm();

  @Test
  void token_bucket_refills_correctly() {
    QuotaState state = new QuotaState();
    state.setTokens(3);
    state.setLastRefill(1000);
    state.setCapacity(10);
    state.setRefillRate(1.0);

    double after = algo.refillFromState(state, 1005);
    assertEquals(8.0, after, 0.001);
  }

  @Test
  void token_bucket_rejects_when_empty() {
    assertFalse(algo.hasTokens(0.5));
    assertTrue(algo.hasTokens(1.0));
    assertEquals(1, algo.estimateRetryAfter(0.2, 1.0));
  }

  @Test
  void refill_caps_at_capacity() {
    QuotaState state = new QuotaState();
    state.setTokens(5);
    state.setLastRefill(1000);
    state.setCapacity(10);
    state.setRefillRate(1.0);

    assertEquals(10.0, algo.refillFromState(state, 2000), 0.001);
  }

  @Test
  void get_refill_rate_from_rule() {
    RateLimitRule rule = baseRule(100, 60);
    assertEquals(100.0 / 60.0, algo.getRefillRate(rule), 0.001);
  }

  private RateLimitRule baseRule(int limit, int window) {
    RateLimitRule rule = new RateLimitRule();
    rule.setId(UUID.randomUUID());
    rule.setTenantId(UUID.randomUUID());
    rule.setName("test");
    rule.setKeyType(KeyType.email);
    rule.setKeyValue("a@b.com");
    rule.setWindowType(WindowType.token_bucket);
    rule.setLimitValue(limit);
    rule.setWindowSizeSeconds(window);
    rule.setEnabled(true);
    return rule;
  }
}
