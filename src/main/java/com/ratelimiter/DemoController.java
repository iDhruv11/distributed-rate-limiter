package com.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

// Endpoint used only by the demo UI.
@RestController
@RequestMapping("/demo")
public class DemoController {

    // Latencies are how long each single check took inside the server (one Redis round trip), in milliseconds.
    record ConcurrencyResult(int requests, int allowed, int denied, long tookMs,
                             double p50Ms, double p95Ms, double p99Ms, double maxMs) {
    }

    private static final Set<Integer> SIZES = Set.of(100, 200, 500);

    private final RuleService rules;
    private final Buckets buckets;
    // One thread per request so they really run at the same time. Threads are created on first use and reused;
    // the lock means only one demo run uses them at a time.
    private final ExecutorService pool = Executors.newFixedThreadPool(500);
    private final ReentrantLock running = new ReentrantLock();

    public DemoController(RuleService rules, Buckets buckets) {
        this.rules = rules;
        this.buckets = buckets;
    }

    // Fires `requests` checks at the same full bucket at once. With capacity N, N get through, plus whatever refills
    // during the run (nothing for a rule like 10/minute, a few for something like 50/second).
    @PostMapping("/concurrency/{ruleId}")
    ConcurrencyResult concurrency(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID ruleId,
                                  @RequestParam(defaultValue = "100") int requests,
                                  HttpServletRequest request) throws Exception {
        if (!SIZES.contains(requests)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requests must be 100, 200 or 500");
        }
        // This endpoint is public and fairly heavy, so it's rate limited by the rate limiter itself: 10 runs a minute per IP.
        if (!buckets.check("rl:demo:" + clientIp(request), 10, 60).allowed()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "too many demo runs, try again in a minute");
        }
        if (!running.tryLock()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "another demo run is in progress, try again");
        }
        try {
            Rule rule = rules.get(tenantId, ruleId);
            String key = Buckets.key(tenantId, ruleId, "concurrency-demo");
            buckets.reset(key);

            // Every thread waits at the start line, then they all go together.
            CountDownLatch go = new CountDownLatch(1);
            List<Future<double[]>> results = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                results.add(pool.submit(() -> {
                    go.await();
                    long t = System.nanoTime();
                    boolean allowed = buckets.check(key, rule.getCapacity(), rule.getWindowSeconds()).allowed();
                    return new double[]{allowed ? 1 : 0, (System.nanoTime() - t) / 1_000_000.0};
                }));
            }
            long start = System.nanoTime();
            go.countDown();

            int allowed = 0;
            double[] ms = new double[requests];
            for (int i = 0; i < requests; i++) {
                double[] r = results.get(i).get();
                allowed += (int) r[0];
                ms[i] = r[1];
            }
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            Arrays.sort(ms);
            return new ConcurrencyResult(requests, allowed, requests - allowed, tookMs,
                    round(pct(ms, 0.50)), round(pct(ms, 0.95)), round(pct(ms, 0.99)), round(ms[requests - 1]));
        } finally {
            running.unlock();
        }
    }

    private static double pct(double[] sorted, double p) {
        return sorted[(int) Math.ceil(p * sorted.length) - 1];
    }

    private static double round(double ms) {
        return Math.round(ms * 100) / 100.0;
    }

    // On the server, Cloudflare puts the visitor's real IP in CF-Connecting-IP (visitors can't fake it through Cloudflare).
    private static String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("CF-Connecting-IP");
        return ip == null ? request.getRemoteAddr() : ip;
    }
}
