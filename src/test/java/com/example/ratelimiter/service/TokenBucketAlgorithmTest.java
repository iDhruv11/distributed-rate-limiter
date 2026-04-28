package com.example.ratelimiter.service;

import com.example.ratelimiter.entity.RateLimitRule;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TokenBucketAlgorithmTest {
    private final TokenBucketAlgorithm algo = new TokenBucketAlgorithm();

    @Test
    void reset_at_is_window_based() {
        RateLimitRule rule = new RateLimitRule();
        rule.setWindowSizeSeconds(60);
        assertEquals(1000 + 60, algo.calculateResetAt(rule, 1000));
    }
}
