package com.example.ratelimiter.dto;

import java.time.Instant;
import java.util.UUID;

public class ApiWrapper<T> {

    private T data;
    private String timestamp;
    private String requestId;

    public ApiWrapper() {
    }

    public ApiWrapper(T data) {
        this.data = data;
        this.timestamp = Instant.now().toString();
        this.requestId = UUID.randomUUID().toString();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
