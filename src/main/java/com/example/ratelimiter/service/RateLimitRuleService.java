package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.CreateRuleRequest;
import com.example.ratelimiter.dto.RuleResponse;
import com.example.ratelimiter.dto.UpdateRuleRequest;
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
    private final public RateLimitRuleService(RateLimitRuleRepository repo) {
        this.repo = repo;
    }

    public RateLimitRule getRuleById(UUID tenantId, UUID ruleId) {
        return repo.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new RuleNotFoundException("rule not found"));
    }

    public List<RuleResponse> listRules(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RuleResponse createRule(UUID tenantId, CreateRuleRequest req) {
        RateLimitRule rule = new RateLimitRule();
        rule.setId(UUID.randomUUID());
        rule.setTenantId(tenantId);
        rule.setName(req.getName());
        rule.setKeyType(req.getKeyType());
        rule.setWindowType(WindowType.token_bucket);
        rule.setLimitValue(req.getCapacity());
        rule.setWindowSizeSeconds(req.getWindowSizeSeconds());
        rule.setEnabled(true);
        rule.setDescription(req.getDescription());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        repo.save(rule);
        return toResponse(rule);
    }

    public RuleResponse updateRule(UUID tenantId, UUID ruleId, UpdateRuleRequest req) {
        RateLimitRule rule = getRuleById(tenantId, ruleId);
        rule.setLimitValue(req.getCapacity());
        rule.setWindowSizeSeconds(req.getWindowSizeSeconds());
        rule.setUpdatedAt(LocalDateTime.now());
        repo.save(rule);

return toResponse(rule);
    }

    public void deleteRule(UUID tenantId, UUID ruleId) {
        RateLimitRule rule = getRuleById(tenantId, ruleId);
        repo.delete(rule);
    }

    public RuleResponse toResponse(RateLimitRule rule) {
        RuleResponse r = new RuleResponse();
        r.setId(rule.getId());
        r.setTenantId(rule.getTenantId());
        r.setName(rule.getName());
        r.setKeyType(rule.getKeyType());
        r.setKeyValue(rule.getKeyValue());
        r.setWindowType(rule.getWindowType());
        r.setCapacity(rule.getLimitValue());
        r.setWindowSizeSeconds(rule.getWindowSizeSeconds());
        r.setEnabled(rule.getEnabled());
        r.setDescription(rule.getDescription());
        r.setCreatedAt(rule.getCreatedAt());
        r.setUpdatedAt(rule.getUpdatedAt());
        return r;
    }
}
