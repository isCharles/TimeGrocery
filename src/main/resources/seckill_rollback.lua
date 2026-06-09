local voucherId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

local removed = redis.call('SREM', orderKey, userId)
if (removed == 1) then
    redis.call('INCRBY', stockKey, 1)
    return 0
end

return 1