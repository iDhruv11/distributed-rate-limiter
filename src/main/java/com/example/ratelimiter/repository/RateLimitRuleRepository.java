package com.example.ratelimiter.repository;

import com.example.ratelimiter.entity.RateLimitRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, UUID> {

    List<RateLimitRule> findByTenantId(UUID tenantId);

    List<RateLimitRule> findByTenantIdAndName(UUID tenantId, String name);

    Optional<RateLimitRule> findByIdAndTenantId(UUID id, UUID tenantId);
}
