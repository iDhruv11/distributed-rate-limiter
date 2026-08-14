package com.ratelimiter;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

// Runs against a real Postgres and a real Redis in Docker, so the Lua script itself is what's being tested.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RateLimiterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    RuleService rules;
    @Autowired
    Buckets buckets;
    @Autowired
    TestRestTemplate http;

    final UUID tenant = UUID.randomUUID();

    Buckets.Result check(Rule rule, String key) {
        return buckets.check(Buckets.key(tenant, rule.getId(), key), rule.getCapacity(), rule.getWindowSeconds());
    }

    @Test
    @Order(1)
    void exactlyCapacityAllowedWhen100HitAtOnce() throws Exception {
        Rule rule = rules.create(tenant, "burst", 10, 3600);
        ExecutorService pool = Executors.newFixedThreadPool(100);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            results.add(pool.submit(() -> {
                go.await();
                return check(rule, "user:1").allowed();
            }));
        }
        go.countDown();
        int allowed = 0;
        for (Future<Boolean> r : results) if (r.get()) allowed++;
        pool.shutdown();
        assertThat(allowed).isEqualTo(10);
    }

    @Test
    @Order(2)
    void eachKeyHasItsOwnBucket() {
        Rule rule = rules.create(tenant, "per-user", 1, 3600);
        assertThat(check(rule, "user:a").allowed()).isTrue();
        assertThat(check(rule, "user:a").allowed()).isFalse();
        assertThat(check(rule, "user:b").allowed()).isTrue();
    }

    @Test
    @Order(3)
    void refillsOverTime() throws Exception {
        Rule rule = rules.create(tenant, "fast", 2, 1); // 2 tokens per second
        check(rule, "k");
        check(rule, "k");
        Buckets.Result denied = check(rule, "k");
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.retryAfterMs()).isBetween(1L, 500L);
        Thread.sleep(600);
        assertThat(check(rule, "k").allowed()).isTrue();
    }

    @Test
    @Order(4)
    void editingARuleCapsTheBucketButNeverRefillsIt() {
        Rule rule = rules.create(tenant, "edit", 10, 3600);
        assertThat(check(rule, "k").remaining()).isEqualTo(9);

        // Lowering capacity caps the bucket on its next check.
        Rule smaller = rules.update(tenant, rule.getId(), "edit", 3, 3600);
        assertThat(check(smaller, "k").remaining()).isEqualTo(2);
        // Raising it again doesn't refill the bucket (the old version handed out a full bucket on every edit).
        Rule bigger = rules.update(tenant, rule.getId(), "edit", 10, 3600);
        assertThat(check(bigger, "k").remaining()).isEqualTo(1);
    }

    @Test
    @Order(5)
    void apiErrorsAreReadable() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ResponseEntity<Map> noHeader = http.exchange("/api/v1/limits/check", HttpMethod.POST,
                new HttpEntity<>("{}", headers), Map.class);
        assertThat(noHeader.getStatusCode().value()).isEqualTo(400);
        assertThat(noHeader.getBody()).containsEntry("error", "X-Tenant-Id header is required");

        headers.set("X-Tenant-Id", tenant.toString());
        ResponseEntity<Map> unknownRule = http.exchange("/api/v1/limits/check", HttpMethod.POST,
                new HttpEntity<>("{\"ruleId\":\"" + UUID.randomUUID() + "\",\"key\":\"u\"}", headers), Map.class);
        assertThat(unknownRule.getStatusCode().value()).isEqualTo(404);
        assertThat(unknownRule.getBody()).containsEntry("error", "rule not found");

        ResponseEntity<Map> invalid = http.exchange("/api/v1/limits", HttpMethod.POST,
                new HttpEntity<>("{\"name\":\"x\",\"capacity\":0,\"windowSeconds\":60}", headers), Map.class);
        assertThat(invalid.getStatusCode().value()).isEqualTo(400);
        assertThat(invalid.getBody()).containsEntry("error", "capacity must be greater than or equal to 1");
    }

    @Test
    @Order(99) // last: it stops Redis
    void failsOpenWhenRedisIsDown() {
        Rule rule = rules.create(tenant, "outage", 1, 3600);
        redis.stop();
        Buckets.Result result = check(rule, "k");
        assertThat(result.redisDown()).isTrue();
        assertThat(result.allowed()).isTrue();
    }
}
