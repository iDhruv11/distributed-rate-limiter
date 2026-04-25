package com.example.ratelimiter.service;

import com.example.ratelimiter.entity.RateLimitRule;

public class TokenBucketAlgorithm {
    public long calculateResetAt(RateLimitRule rule, long now) {
        return now + rule.getWindowSizeSeconds();
    }
}
