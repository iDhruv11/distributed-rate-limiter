package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.QuotaState;
import com.example.ratelimiter.entity.RateLimitRule;

public class TokenBucketAlgorithm {

    public double getRefillRate(RateLimitRule rule) {
        return (double) rule.getLimitValue() / rule.getWindowSizeSeconds();
    }

    public double refillFromState(QuotaState state, long now) {
        double elapsed = now - state.getLastRefill();
        if (elapsed < 0) {
            elapsed = 0;
        }
        double tokens = state.getTokens() + (elapsed * state.getRefillRate());
        if (tokens > state.getCapacity()) {
            tokens = state.getCapacity();
        }
        return tokens;
    }

    public long estimateFullAt(double tokens, int capacity, double rate, long now) {
        if (tokens >= capacity) {
            return now;
        }
        double needed = capacity - tokens;
        return now + (long) Math.ceil(needed / rate);
    }

    public long estimateRetryAfter(double tokens, double rate) {
        if (tokens >= 1) {
            return 0;
        }
        return (long) Math.ceil((1 - tokens) / rate);
    }

    public boolean hasTokens(double tokens) {
        return tokens >= 1;
    }
}
