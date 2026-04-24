package com.example.ratelimiter.service;

public interface RateLimitingAlgorithm {
    boolean checkAndDecrement();
}
