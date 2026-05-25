package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ApiWrapper;
import com.example.ratelimiter.dto.ConcurrencyResult;
import com.example.ratelimiter.dto.RuleResponse;
import com.example.ratelimiter.service.RateLimitRuleService;
import com.example.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.example.ratelimiter.service.RedisLuaScriptService;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final RateLimitRuleService ruleService;
    private final RateLimiterService rateLimiterService;
    private final RedisLuaScriptService redisLua;

    private final String demoName;

    public DemoController(RateLimitRuleService ruleService,
                          RateLimiterService rateLimiterService,
                          RedisLuaScriptService redisLua,
                          @Value("${ratelimiter.demo.name:acme-demo}") String demoName) {
        this.ruleService = ruleService;
        this.rateLimiterService = rateLimiterService;
        this.redisLua = redisLua;
        this.demoName = demoName;
    }
    
    @GetMapping("/redis-health")
    public ApiWrapper<Map<String, Boolean>> redisHealth() {
        Map<String, Boolean> out = new HashMap<>();
        out.put("up", redisLua.ping());
        return new ApiWrapper<>(out);
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
