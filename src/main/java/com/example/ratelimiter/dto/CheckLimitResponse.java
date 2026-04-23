package com.example.ratelimiter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckLimitResponse {

    private boolean allowed;

    @JsonProperty("remainingTokens")
    private Long remainingTokens;

    private Long remainingQuota;

    @JsonProperty("refillRate")
    private Double refillRate;

    private Long resetAt;
    private Integer windowSeconds;
    private String reason;
    private Long retryAfterSeconds;
    private boolean initialized;

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public Long getRemainingTokens() {
        return remainingTokens;
    }

    public void setRemainingTokens(Long remainingTokens) {
        this.remainingTokens = remainingTokens;
    }

    public Long getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(Long remainingQuota) {
        this.remainingQuota = remainingQuota;
        this.remainingTokens = remainingQuota;
    }

    public Double getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Double refillRate) {
        this.refillRate = refillRate;
    }

    public Long getResetAt() {
        return resetAt;
    }

    public void setResetAt(Long resetAt) {
        this.resetAt = resetAt;
    }

    public Integer getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Integer windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public void setRetryAfterSeconds(Long retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
