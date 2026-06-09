local voucherId = ARGV[1]
local userId = ARGV[2]
--local orderId = ARGV[3]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 判断库存是否充足
local stock = redis.call('GET', stockKey)
if (not stock or stock == '' or tonumber(stock) <= 0) then
    return 1
end
-- 判断用户是否已经下单
if (redis.call('SISMEMBER', orderKey, userId) == 1) then
    return 2
end
-- 扣库存（使用 tonumber 确保是整数）
local newStock = redis.call('DECRBY', stockKey, 1)
if (newStock < 0) then
    redis.call('INCRBY', stockKey, 1)
    return 1
end
-- 下单
redis.call('SADD', orderKey, userId)
return 0
