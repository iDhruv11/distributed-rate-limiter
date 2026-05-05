local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local window_size = tonumber(ARGV[3])
local key_type = ARGV[4]
local key_value = ARGV[5]
local now = tonumber(ARGV[6])

local bucket = {}
bucket.tokens = capacity
bucket.last_refill = now
bucket.capacity = capacity
bucket.refill_rate = refill_rate
bucket.window_size_seconds = window_size
bucket.key_type = key_type
bucket.key_value = key_value
bucket.algorithm = "token_bucket"
bucket.allowed = 1

local ttl = math.ceil(capacity / refill_rate) + 120
if ttl < 60 then
  ttl = 60
end
redis.call('SETEX', key, ttl, cjson.encode(bucket))

return cjson.encode(bucket)
