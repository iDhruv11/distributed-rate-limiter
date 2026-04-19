package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.CreateRuleRequest;
import com.example.ratelimiter.dto.RuleResponse;
import com.example.ratelimiter.service.RateLimitRuleService;
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
    public RuleResponse create(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CreateRuleRequest req) {
        return ruleService.createRule(tenantId, req);
    }

    @GetMapping
    public List<RuleResponse> list(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ruleService.listRules(tenantId);
    }
}
