# Distributed Rate Limiter

A standalone rate-limiting service that multiple backend apps call instead of rolling their own limits. You send a tenant ID + rule ID, you get back allowed/denied, remaining tokens, and reset info — all coordinated across instances with no race conditions.

**What makes this interesting:**
- Token bucket algorithm with **atomic Redis Lua scripts** (check + refill + decrement in one shot)
- **Sub-10ms p99** on the hot check path (~1–2ms per Redis hit in practice)
- **100 concurrent requests → exactly N allowed** where N is remaning requests or max capcity whichever less (proves atomicity)
- **Resilience4j circuit breaker** with fail-open or fail-closed when Redis goes down
- Multi-tenant isolation via `X-Tenant-Id` header
- React demo UI baked into the same JAR so you can poke at it without writing curl commands

---

## Who is this for?

Backend teams that need centralized rate limiting across services — per user, API key, IP, email. Instead of every app maintaining its own counters (and getting them wrong under concurrency), they hit one service.

---

## How a tenant would use it

```
  Tenant's app                          Rate Limiter Service
       |                                        |
       |  1. POST /api/v1/limits                |
       |     (create rule for a user/key)      |
       |--------------------------------------->|
       |     returns rule_id                   |
       |<---------------------------------------|
       |                                        |
       |  store rule_id in your users table     |
       |                                        |
       |  2. POST /api/v1/limits/check          |
       |     X-Tenant-Id + ruleId              |
       |--------------------------------------->|
       |     allowed, remainingTokens, etc.    |
       |<---------------------------------------|
       |                                        |
       |  3. GET /api/v1/limits/{id}/status    |  (peek, no decrement)
       |  4. POST /api/v1/rules/{id}/reset     |  (manual bucket reset)
       |  5. PUT /api/v1/limits/{id}           |  (change capacity/window)
       |  6. DELETE /api/v1/limits/{id}        |  (remove rule + redis key)
```

**Typical onboarding flow:**

1. Tenant creates one row per thing they want to limit (user, API key, IP, etc.) via `POST /api/v1/limits`. Each row gets its own UUID — that's the `rule_id`.
2. Tenant stores that `rule_id` however they want (e.g. a column on their users table).
3. When a request comes in, tenant's backend calls `POST /api/v1/limits/check` with their tenant UUID and the rule ID. First call bootstraps Redis state from Postgres; after that Postgres stays off the hot path.
4. If they change limits, `PUT` updates Postgres and re-seeds Redis to full capacity atomically.

No auth/JWT/onboarding in this project — tenants are identified purely by the `X-Tenant-Id` header. Redis keys are scoped as `rate_limit:{tenant_id}:{rule_id}` so tenants never bleed into each other.

---

## Architecture

```
  Client apps                Demo UI (React)
       |                           |
       +-----------+---------------+
                   |
                   v
         Spring Boot API (Java 17)
                   |
         +---------+---------+
         |                   |
         v                   v
    PostgreSQL             Redis
    (rule config)     (live quota state)
                           |
                      Lua scripts
                   (atomic read/refill/decrement)
```

**Split of responsibility:**
- **PostgreSQL** — rule definitions only (capacity, window, key type, enabled flag). Not touched on every check.
- **Redis** — the live token bucket state. Source of truth for quota. If Redis data is lost, quota resets — rules in Postgres are fine.

**Why Lua?** Separate GET → check → SET calls race under concurrency. Two threads both read `tokens=1`, both pass, bucket goes negative. Lua runs the whole thing atomically inside Redis.

**Token bucket behavior:** Tokens refill continuously at `capacity / window_seconds` per second. Each allowed request burns one token. Denied when empty.

---

## API overview

All tenant routes need header: `X-Tenant-Id: <uuid>`

| Method | Path | What it does |
|--------|------|--------------|
| POST | `/api/v1/limits` | Create a rate limit rule |
| PUT | `/api/v1/limits/{ruleId}` | Update capacity/window, re-seed Redis |
| DELETE | `/api/v1/limits/{ruleId}` | Delete rule from DB + Redis |
| POST | `/api/v1/limits/check` | **Hot path** — check and decrement atomically |
| GET | `/api/v1/limits/{ruleId}/status` | Read current bucket state (no decrement) |
| POST | `/api/v1/rules/{ruleId}/reset` | Wipe Redis key, fresh bucket on next check |

Check response includes: `allowed`, `remainingTokens`, `refillRate`, `resetAt`, `windowSeconds`, `reason`, `retryAfterSeconds`.

Demo helpers (used by the UI):

| Method | Path | What it does |
|--------|------|--------------|
| GET | `/demo/redis-health` | Ping Redis, returns `{ up: true/false }` |
| GET | `/demo/rules` | List demo rules for the hardcoded tenant |
| POST | `/demo/concurrency/{ruleId}` | Fire 100 parallel checks, return allowed/denied counts + timing |

OpenAPI spec at `/api-docs`.

---

## Demo UI pages

The React app ships inside the Spring Boot JAR and is served on `/`.

| Page | Route | Purpose |
|------|-------|---------|
| Create Rule | `/` | Make a new rule (demo tenant, name locked to `acme-demo`) |
| Live Test | `/live` | Send 1 or 10 check requests, see tokens drop, reset quota |
| Concurrency | `/concurrency` | **Main demo** — 100 parallel requests, shows allowed vs denied |
| Redis State | `/state` | View current bucket JSON without consuming tokens |
| Manage Rules | `/manage` | Edit capacity/window or delete rules (syncs DB + Redis) |

Redis health badge (green/red dot) on pages that depend on Redis.

---

## Tech stack

**Backend**
- Java 17, Spring Boot 3.2.5
- Spring Data JPA + Flyway (PostgreSQL 16)
- Spring Data Redis + Lua scripting
- Resilience4j circuit breaker
- Springdoc OpenAPI
- Maven

**Frontend**
- React 18, TypeScript, Vite
- Tailwind CSS, Radix UI primitives
- React Router

**Infrastructure**
- Docker multi-stage build (Node → Maven → JRE Alpine)
- Docker Compose: app + Postgres + Redis
- Memory limits tuned for ~2GB VMs (app 768M, postgres 256M, redis 128M)

---

## Performance and reliability

| Target / observation | Value |
|---------------------|-------|
| p99 latency goal | sub-10ms |
| Observed per-check Redis latency | ~1–2ms |
| 100 concurrent checks (capacity 10) | 10 allowed, 90 denied — every time |
| 100 concurrent checks wall time | ~155ms average |
| Redis command timeout | 2s |
| Circuit breaker | 5-call sliding window, 50% failure threshold, 5s open state |

- **Fail-open:** checks return `allowed: true` (availability over enforcement)
- **Fail-closed:** checks return `allowed: false`, reason `redis_unavailable`

---

## Production deployment notes

Deployed on a 2GB Azure VM running this alongside another app. Docker Compose memory limits keep the stack from getting OOM-killed. A Logic App workflow stops the VM at 19:00 and starts it at 09:00 daily to save cost. systemd brings up nginx (port 80) and docker compose on boot. Cloudflare handles DNS/subdomains; a Worker shows a maintenance page during the nightly shutdown window. nginx routes subdomains to the right internal ports.

---

## Project structure

```
src/main/java/com/example/ratelimiter/
  controller/     REST endpoints
  service/        RateLimiterService, RedisLuaScriptService, TokenBucketAlgorithm
  entity/         JPA models
  dto/            Request/response objects
  exception/      Global error handling

src/main/resources/
  lua/            check_and_decrement.lua, init_state.lua
  db/migration/   Flyway schema

frontend/         React demo UI
```
