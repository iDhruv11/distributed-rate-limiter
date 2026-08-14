package com.ratelimiter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID> {

    List<Rule> findByTenantIdOrderByCreatedAt(UUID tenantId);

    Optional<Rule> findByIdAndTenantId(UUID id, UUID tenantId);
}
