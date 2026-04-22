local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window_size = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local state = redis.call('GET', key)
local quota_data = {}

if not state then
  quota_data.current_quota = limit - 1
  quota_data.reset_at = now + window_size
  quota_data.allowed = 1
else
  quota_data = cjson.decode(state)
  if now >= quota_data.reset_at then
    quota_data.current_quota = limit - 1
    quota_data.reset_at = now + window_size
    quota_data.allowed = 1
  else
    if quota_data.current_quota > 0 then
      quota_data.current_quota = quota_data.current_quota - 1
      quota_data.allowed = 1
    else
      quota_data.allowed = 0
    end
  end
end

local ttl = quota_data.reset_at - now + 1
redis.call('SETEX', key, ttl, cjson.encode(quota_data))
return cjson.encode(quota_data)
