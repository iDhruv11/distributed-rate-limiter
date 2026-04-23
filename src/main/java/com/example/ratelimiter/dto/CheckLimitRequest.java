package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.KeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CheckLimitRequest {
    @NotNull private UUID ruleId;
    @NotBlank private String keyValue;
    @NotNull private KeyType keyType;

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }
    public KeyType getKeyType() { return keyType; }
    public void setKeyType(KeyType keyType) { this.keyType = keyType; }
}
