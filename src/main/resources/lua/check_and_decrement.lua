local key = KEYS[1]
local now = tonumber(ARGV[1])

local state = redis.call('GET', key)
local bucket = {}

if not state then
  local capacity = tonumber(ARGV[2])
  local refill_rate = tonumber(ARGV[3])
  local window_size = tonumber(ARGV[4])
  local key_type = ARGV[5]
  local key_value = ARGV[6]

  bucket.tokens = capacity - 1
  bucket.last_refill = now
  bucket.capacity = capacity
  bucket.refill_rate = refill_rate
  bucket.window_size_seconds = window_size
  bucket.key_type = key_type
  bucket.key_value = key_value
  bucket.allowed = 1
else
  bucket = cjson.decode(state)
  local capacity = bucket.capacity
  local refill_rate = bucket.refill_rate
  local elapsed = now - bucket.last_refill
  if elapsed < 0 then
    elapsed = 0
  end
  local tokens = bucket.tokens + (elapsed * refill_rate)
  if tokens > capacity then
    tokens = capacity
  end
  if tokens >= 1 then
    tokens = tokens - 1
    bucket.allowed = 1
  else
    bucket.allowed = 0
  end
  bucket.tokens = tokens
  bucket.last_refill = now
end

bucket.algorithm = "token_bucket"

local ttl = math.ceil(bucket.capacity / bucket.refill_rate) + 120
if ttl < 60 then
  ttl = 60
end
redis.call('SETEX', key, ttl, cjson.encode(bucket))

return cjson.encode(bucket)
