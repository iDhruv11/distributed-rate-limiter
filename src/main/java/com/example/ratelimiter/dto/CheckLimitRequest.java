package com.example.ratelimiter.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CheckLimitRequest {

    @NotNull
    private UUID ruleId;

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }
}
