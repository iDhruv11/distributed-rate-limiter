-- Token bucket, run atomically inside Redis so concurrent checks can't race.
-- KEYS[1] = bucket key, ARGV[1] = capacity, ARGV[2] = window seconds.
-- The bucket is a hash {tokens, ts}. A missing bucket counts as full, so there is no separate "create" step.
local capacity = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local rate = capacity / window -- tokens added per second

-- Use Redis's clock, not the app server's, so every app instance agrees on the time.
local t = redis.call('TIME')
local now = tonumber(t[1]) + tonumber(t[2]) / 1000000

local saved = redis.call('HMGET', KEYS[1], 'tokens', 'ts')
local tokens = tonumber(saved[1]) or capacity
local ts = tonumber(saved[2]) or now

if now > ts then
  tokens = tokens + (now - ts) * rate
  ts = now
end
-- Also caps the bucket right away if the rule's capacity was lowered.
tokens = math.min(tokens, capacity)

local allowed = 0
if tokens >= 1 then
  allowed = 1
  tokens = tokens - 1
end

redis.call('HSET', KEYS[1], 'tokens', tokens, 'ts', ts)
-- An idle bucket is full again after `window` seconds, and a missing bucket means full, so it can expire then.
redis.call('EXPIRE', KEYS[1], math.ceil(window) + 60)

-- Lua numbers become integers when returned to Redis, so send tokens as a string to keep the fraction.
return {allowed, tostring(tokens)}
