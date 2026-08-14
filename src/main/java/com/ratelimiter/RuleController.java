package com.ratelimiter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Every request names its tenant in the X-Tenant-Id header. There's no login in this project, so the header is trusted.
@RestController
@RequestMapping("/api/v1/limits")
public class RuleController {

    record RuleRequest(@NotBlank @Size(max = 100) String name,
                       @NotNull @Min(1) @Max(1_000_000) Integer capacity,
                       @NotNull @Min(1) @Max(86_400) Integer windowSeconds) {
    }

    // key is whatever you're limiting: "user:42", an API key, an IP address...
    record CheckRequest(@NotNull UUID ruleId, @NotBlank @Size(max = 200) String key) {
    }

    private final RuleService rules;
    private final Buckets buckets;

    public RuleController(RuleService rules, Buckets buckets) {
        this.rules = rules;
        this.buckets = buckets;
    }

    @GetMapping
    List<Rule> list(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return rules.list(tenantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Rule create(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody RuleRequest body) {
        return rules.create(tenantId, body.name(), body.capacity(), body.windowSeconds());
    }

    @PutMapping("/{ruleId}")
    Rule update(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID ruleId, @Valid @RequestBody RuleRequest body) {
        return rules.update(tenantId, ruleId, body.name(), body.capacity(), body.windowSeconds());
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID ruleId) {
        rules.delete(tenantId, ruleId);
    }

    // The hot path: one cached rule lookup + one Redis script call. Always 200; the caller decides what to do with
    // allowed=false (usually send its own 429 with retryAfterMs as Retry-After).
    @PostMapping("/check")
    Buckets.Result check(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CheckRequest body) {
        Rule rule = rules.getCached(tenantId, body.ruleId());
        return buckets.check(Buckets.key(tenantId, rule.getId(), body.key()), rule.getCapacity(), rule.getWindowSeconds());
    }

    // Deleting the bucket = full bucket on the next check.
    @PostMapping("/{ruleId}/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID ruleId, @RequestParam String key) {
        Rule rule = rules.get(tenantId, ruleId);
        buckets.reset(Buckets.key(tenantId, rule.getId(), key));
    }
}
