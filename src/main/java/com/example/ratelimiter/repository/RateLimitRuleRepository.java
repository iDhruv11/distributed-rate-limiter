package com.example.ratelimiter.repository;

import com.example.ratelimiter.entity.RateLimitRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, UUID> {
    List<RateLimitRule> findByTenantId(UUID tenantId);
}
