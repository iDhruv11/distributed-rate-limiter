package com.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Rules live in Postgres. Checks read them from a small in-memory cache so Postgres isn't hit on every request.
@Service
public class RuleService {

    private static final Logger log = LoggerFactory.getLogger(RuleService.class);

    // With several app instances, an edit made on another instance shows up here within 30 seconds.
    private static final long CACHE_MILLIS = 30_000;

    private record Cached(Rule rule, long loadedAt) {
    }

    private final RuleRepository repo;
    private final Map<UUID, Cached> cache = new ConcurrentHashMap<>();

    public RuleService(RuleRepository repo) {
        this.repo = repo;
    }

    public List<Rule> list(UUID tenantId) {
        return repo.findByTenantIdOrderByCreatedAt(tenantId);
    }

    public Rule get(UUID tenantId, UUID ruleId) {
        return repo.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "rule not found"));
    }

    public Rule getCached(UUID tenantId, UUID ruleId) {
        Cached cached = cache.get(ruleId);
        if (cached == null || System.currentTimeMillis() - cached.loadedAt() > CACHE_MILLIS) {
            cached = new Cached(get(tenantId, ruleId), System.currentTimeMillis());
            cache.put(ruleId, cached);
        }
        if (!cached.rule().getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "rule not found");
        }
        return cached.rule();
    }

    public Rule create(UUID tenantId, String name, int capacity, int windowSeconds) {
        Rule rule = repo.save(new Rule(tenantId, name, capacity, windowSeconds));
        log.info("created rule {} for tenant {}: {}/{}s", rule.getId(), tenantId, capacity, windowSeconds);
        return rule;
    }

    // Only Postgres changes. Every check sends the rule's capacity and window to Redis, so buckets pick up the new
    // values on their next check (and get capped right away if capacity went down). Nobody gets a free refill.
    public Rule update(UUID tenantId, UUID ruleId, String name, int capacity, int windowSeconds) {
        Rule rule = get(tenantId, ruleId);
        rule.update(name, capacity, windowSeconds);
        repo.save(rule);
        cache.remove(ruleId);
        return rule;
    }

    // Buckets of a deleted rule are left in Redis to expire on their own. Checks against the rule get 404 (on other
    // app instances once their 30 second cache runs out).
    public void delete(UUID tenantId, UUID ruleId) {
        repo.delete(get(tenantId, ruleId));
        cache.remove(ruleId);
        log.info("deleted rule {} for tenant {}", ruleId, tenantId);
    }
}
