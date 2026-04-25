package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ApiWrapper;
import com.example.ratelimiter.dto.CheckLimitRequest;
import com.example.ratelimiter.dto.CheckLimitResponse;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/limits")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/check")
    public ApiWrapper<CheckLimitResponse> check(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CheckLimitRequest req) {
        return new ApiWrapper<>(rateLimiterService.checkLimit(tenantId, req));
    }
}
