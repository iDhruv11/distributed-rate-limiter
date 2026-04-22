package com.example.ratelimiter.dto;

public class QuotaState {
    private int currentQuota;
    private long resetAt;
    private int allowed;

    public int getCurrentQuota() { return currentQuota; }
    public void setCurrentQuota(int currentQuota) { this.currentQuota = currentQuota; }
    public long getResetAt() { return resetAt; }
    public void setResetAt(long resetAt) { this.resetAt = resetAt; }
    public int getAllowed() { return allowed; }
    public void setAllowed(int allowed) { this.allowed = allowed; }
}
