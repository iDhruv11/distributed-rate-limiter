package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.*;
import com.example.ratelimiter.service.RateLimitRuleService;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/limits")
public class RateLimitRuleController {
    private final RateLimitRuleService ruleService;

    public RateLimitRuleController(RateLimitRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public ApiWrapper<RuleResponse> create(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CreateRuleRequest req) {
        return new ApiWrapper<>(ruleService.createRule(tenantId, req));
    }

@PutMapping("/{ruleId}")
    public ApiWrapper<RuleResponse> update(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID ruleId, @Valid @RequestBody UpdateRuleRequest req) {
        return new ApiWrapper<>(ruleService.updateRule(tenantId, ruleId, req));
    }

    @DeleteMapping("/{ruleId}")
    public ApiWrapper<String> delete(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID ruleId) {
        ruleService.deleteRule(tenantId, ruleId);
        return new ApiWrapper<>("deleted");
    }
}
