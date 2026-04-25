package com.example.ratelimiter.exception;

import com.example.ratelimiter.dto.ApiWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ApiWrapper<Map<String, String>>> ruleNotFound(RuleNotFoundException ex) {
        Map<String, String> err = new HashMap<>();
        err.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiWrapper<>(err));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiWrapper<Map<String, String>>> rateLimit(RateLimitExceededException ex) {
        Map<String, String> err = new HashMap<>();
        err.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiWrapper<>(err));
    }

    @ExceptionHandler(RedisUnavailableException.class)
    public ResponseEntity<ApiWrapper<Map<String, String>>> redisDown(RedisUnavailableException ex) {
        Map<String, String> err = new HashMap<>();
        err.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiWrapper<>(err));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiWrapper<Map<String, String>>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> err = new HashMap<>();
        err.put("error", "validation failed");
        return ResponseEntity.badRequest().body(new ApiWrapper<>(err));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiWrapper<Map<String, String>>> badArg(IllegalArgumentException ex) {
        Map<String, String> err = new HashMap<>();
        err.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiWrapper<>(err));
    }
}
