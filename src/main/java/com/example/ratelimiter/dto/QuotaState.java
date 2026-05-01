package com.example.ratelimiter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuotaState {

    private double tokens;

    @JsonProperty("last_refill")
    private long lastRefill;

    private Integer capacity;

    @JsonProperty("refill_rate")
    private Double refillRate;

    @JsonProperty("window_size_seconds")
    private Integer windowSizeSeconds;

    @JsonProperty("key_type")
    private String keyType;

    @JsonProperty("key_value")
    private String keyValue;

    private int allowed;
    private String algorithm;

    public double getTokens() {
        return tokens;
    }

    public void setTokens(double tokens) {
        this.tokens = tokens;
    }

    public long getLastRefill() {
        return lastRefill;
    }

    public void setLastRefill(long lastRefill) {
        this.lastRefill = lastRefill;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Double refillRate) {
        this.refillRate = refillRate;
    }

    public Integer getWindowSizeSeconds() {
        return windowSizeSeconds;
    }

    public void setWindowSizeSeconds(Integer windowSizeSeconds) {
        this.windowSizeSeconds = windowSizeSeconds;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public int getAllowed() {
        return allowed;
    }

    public void setAllowed(int allowed) {
        this.allowed = allowed;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
