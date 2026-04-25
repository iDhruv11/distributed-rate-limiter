package com.example.ratelimiter.exception;

public class RedisUnavailableException extends RuntimeException {

    public RedisUnavailableException(String msg) {
        super(msg);
    }

    public RedisUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
