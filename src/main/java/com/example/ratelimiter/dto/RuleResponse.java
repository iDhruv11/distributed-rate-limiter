package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.KeyType;
import com.example.ratelimiter.entity.WindowType;
import java.time.LocalDateTime;
import java.util.UUID;

public class RuleResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private KeyType keyType;
    private WindowType windowType;
    private Integer limitValue;
    private Integer windowSizeSeconds;
    private Boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public KeyType getKeyType() { return keyType; }
    public void setKeyType(KeyType keyType) { this.keyType = keyType; }
    public WindowType getWindowType() { return windowType; }
    public void setWindowType(WindowType windowType) { this.windowType = windowType; }
    public Integer getLimitValue() { return limitValue; }
    public void setLimitValue(Integer limitValue) { this.limitValue = limitValue; }
    public Integer getWindowSizeSeconds() { return windowSizeSeconds; }
    public void setWindowSizeSeconds(Integer windowSizeSeconds) { this.windowSizeSeconds = windowSizeSeconds; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
