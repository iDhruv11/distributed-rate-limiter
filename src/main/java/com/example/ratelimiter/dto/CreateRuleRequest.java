package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateRuleRequest {

    @NotBlank
    private String name;

    @NotNull
    private KeyType keyType;

    @NotBlank
    private String keyValue;

    @NotNull
    @Min(1)
    @JsonProperty("capacity")
    private Integer capacity;

    @NotNull
    @Min(1)
    private Integer windowSizeSeconds;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getWindowSizeSeconds() {
        return windowSizeSeconds;
    }

    public void setWindowSizeSeconds(Integer windowSizeSeconds) {
        this.windowSizeSeconds = windowSizeSeconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
