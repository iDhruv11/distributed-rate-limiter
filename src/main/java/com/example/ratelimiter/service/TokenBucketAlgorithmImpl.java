package com.example.ratelimiter.service;

public class TokenBucketAlgorithmImpl implements RateLimitingAlgorithm {
    @Override
    public boolean checkAndDecrement() {
        return true;
    }
}
