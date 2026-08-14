package com.ratelimiter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

// One rate limit policy, e.g. "login: 5 requests per 60 seconds". Returned as-is from the API.
@Entity
@Table(name = "rules")
public class Rule {

    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private int capacity;
    private int windowSeconds;
    private Instant createdAt;
    private Instant updatedAt;

    protected Rule() {
    }

    Rule(UUID tenantId, String name, int capacity, int windowSeconds) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.createdAt = Instant.now();
        update(name, capacity, windowSeconds);
    }

    void update(String name, int capacity, int windowSeconds) {
        this.name = name.trim();
        this.capacity = capacity;
        this.windowSeconds = windowSeconds;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getWindowSeconds() { return windowSeconds; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
