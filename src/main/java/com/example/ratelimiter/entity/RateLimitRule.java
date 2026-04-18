package com.example.ratelimiter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rate_limit_rules")
public class RateLimitRule {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type")
    private KeyType keyType;

        @Enumerated(EnumType.STRING)
    @Column(name = "window_type")
    private WindowType windowType;

    @Column(name = "limit_value")
    private Integer limitValue;

    @Column(name = "window_size_seconds")
    private Integer windowSizeSeconds;

    private Boolean enabled;

