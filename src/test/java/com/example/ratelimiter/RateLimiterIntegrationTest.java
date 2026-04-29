package com.example.ratelimiter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterIntegrationTest extends IntegrationTestBase {
    @Test
    void contextLoads() {
        assertTrue(postgres.isRunning());
    }
}
