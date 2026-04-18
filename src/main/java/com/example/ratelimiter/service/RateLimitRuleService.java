package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.CreateRuleRequest;
import com.example.ratelimiter.dto.RuleResponse;
import com.example.ratelimiter.entity.RateLimitRule;
import com.example.ratelimiter.entity.WindowType;
import com.example.ratelimiter.exception.RuleNotFoundException;
import com.example.ratelimiter.repository.RateLimitRuleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RateLimitRuleService {
    private final RateLimitRuleRepository repo;

    public RateLimitRuleService(RateLimitRuleRepository repo) {
        this.repo = repo;
    }

    public RateLimitRule getRuleById(UUID ruleId) {
        return repo.findById(ruleId).orElseThrow(() -> new RuleNotFoundException("rule not found"));
    }

    public List<RuleResponse> listRules(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RuleResponse createRule(UUID tenantId, CreateRuleRequest req) {
        RateLimitRule rule = new RateLimitRule();
        rule.setId(UUID.randomUUID());
        rule.setTenantId(tenantId);
        rule.setName(req.getName());
        rule.setKeyType(req.getKeyType());
        rule.setWindowType(WindowType.token_bucket);
        rule.setLimitValue(req.getLimitValue());
        rule.setWindowSizeSeconds(req.getWindowSizeSeconds());
        rule.setEnabled(true);
        rule.setDescription(req.getDescription());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        repo.save(rule);
        return toResponse(rule);
    }

    private RuleResponse toResponse(RateLimitRule rule) {
        RuleResponse r = new RuleResponse();
        r.setId(rule.getId());
        r.setTenantId(rule.getTenantId());
        r.setName(rule.getName());
        r.setKeyType(rule.getKeyType());
        r.setWindowType(rule.getWindowType());
        r.setLimitValue(rule.getLimitValue());
        r.setWindowSizeSeconds(rule.getWindowSizeSeconds());
        r.setEnabled(rule.getEnabled());
        r.setDescription(rule.getDescription());
        r.setCreatedAt(rule.getCreatedAt());
        r.setUpdatedAt(rule.getUpdatedAt());
        return r;
    }
}
