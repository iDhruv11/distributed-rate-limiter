package com.example.ratelimiter.dto;

public class ConcurrencyResult {

    private int allowed;
    private int denied;

    public int getAllowed() {
        return allowed;
    }

    public void setAllowed(int allowed) {
        this.allowed = allowed;
    }

    public int getDenied() {
        return denied;
    }

    public void setDenied(int denied) {
        this.denied = denied;
    }
}
