package com.example.ratelimiter.exception;

public class RuleNotFoundException extends RuntimeException {

    public RuleNotFoundException(String msg) {
        super(msg);
    }
}
