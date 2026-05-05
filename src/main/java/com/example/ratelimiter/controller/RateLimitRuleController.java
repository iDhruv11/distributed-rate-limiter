package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ApiWrapper;
import com.example.ratelimiter.dto.CheckLimitRequest;
import com.example.ratelimiter.dto.CheckLimitResponse;
import com.example.ratelimiter.dto.CreateRuleRequest;
import com.example.ratelimiter.dto.RuleResponse;
import com.example.ratelimiter.dto.UpdateRuleRequest;
import com.example.ratelimiter.service.RateLimitRuleService;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/limits")
public class RateLimitRuleController {

    private final RateLimitRuleService ruleService;
    private final RateLimiterService rateLimiterService;

    public RateLimitRuleController(RateLimitRuleService ruleService, RateLimiterService rateLimiterService) {
        this.ruleService = ruleService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping
    public ApiWrapper<RuleResponse> create(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody CreateRuleRequest req) {
        return new ApiWrapper<>(ruleService.createRule(tenantId, req));
    }

    @PutMapping("/{ruleId}")
    public ApiWrapper<RuleResponse> update(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateRuleRequest req) {
        return new ApiWrapper<>(ruleService.updateRule(tenantId, ruleId, req));
    }

    @DeleteMapping("/{ruleId}")
    public ApiWrapper<String> delete(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ruleId) {
        ruleService.deleteRule(tenantId, ruleId);
        return new ApiWrapper<>("deleted");
    }

    @PostMapping("/check")
    public ApiWrapper<CheckLimitResponse> check(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody CheckLimitRequest req) {
        return new ApiWrapper<>(rateLimiterService.checkLimit(tenantId, req.getRuleId()));
    }

    @GetMapping("/{ruleId}/status")
    public ApiWrapper<CheckLimitResponse> status(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ruleId) {
        return new ApiWrapper<>(rateLimiterService.getQuotaRemaining(tenantId, ruleId));
    }
}
