package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.KeyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateRuleRequest {
    @NotBlank private String name;
    @NotNull private KeyType keyType;
    @NotNull @Min(1) private Integer limitValue;
    @NotNull @Min(1) private Integer windowSizeSeconds;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public KeyType getKeyType() { return keyType; }
    public void setKeyType(KeyType keyType) { this.keyType = keyType; }
    public Integer getLimitValue() { return limitValue; }
    public void setLimitValue(Integer limitValue) { this.limitValue = limitValue; }
    public Integer getWindowSizeSeconds() { return windowSizeSeconds; }
    public void setWindowSizeSeconds(Integer windowSizeSeconds) { this.windowSizeSeconds = windowSizeSeconds; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
