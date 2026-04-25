package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ApiWrapper;
import com.example.ratelimiter.service.RateLimiterService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleResetController {

    private final RateLimiterService rateLimiterService;

    public RuleResetController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/{ruleId}/reset")
    public ApiWrapper<String> reset(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ruleId) {
        rateLimiterService.resetQuota(tenantId, ruleId);
        return new ApiWrapper<>("reset ok");
    }
}
