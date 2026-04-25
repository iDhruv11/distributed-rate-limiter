package com.example.ratelimiter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateRuleRequest {

    @NotNull
    @Min(1)
    @JsonProperty("capacity")
    private Integer capacity;

    @NotNull
    @Min(1)
    private Integer windowSizeSeconds;

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
}
