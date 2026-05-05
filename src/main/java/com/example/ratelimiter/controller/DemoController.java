package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ApiWrapper;
import com.example.ratelimiter.dto.ConcurrencyResult;
import com.example.ratelimiter.dto.RuleResponse;
import com.example.ratelimiter.service.RateLimitRuleService;
import com.example.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final RateLimitRuleService ruleService;
    private final RateLimiterService rateLimiterService;
    private final String demoName;

    public DemoController(RateLimitRuleService ruleService,
                          RateLimiterService rateLimiterService,
                          @Value("${ratelimiter.demo.name:acme-demo}") String demoName) {
        this.ruleService = ruleService;
        this.rateLimiterService = rateLimiterService;
        this.demoName = demoName;
    }

    @GetMapping("/rules")
    public ApiWrapper<List<RuleResponse>> listRules(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return new ApiWrapper<>(ruleService.listDemoRules(tenantId, demoName));
    }

    @PostMapping("/concurrency/{ruleId}")
    public ApiWrapper<ConcurrencyResult> concurrency(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ruleId) {
        return new ApiWrapper<>(rateLimiterService.runConcurrencyDemo(tenantId, ruleId));
    }
}
