package com.example.ratelimiter;

import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.jupiter.api.BeforeAll;

public abstract class IntegrationTestBase {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void start() {
        postgres.start();
    }
}
