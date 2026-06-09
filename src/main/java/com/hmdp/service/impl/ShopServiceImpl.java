package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private Cache<Long,Shop> shopLocalCache;

    @Value("${hmdp.cache.local.enabled:true}")
    private boolean localCacheEnabled;

    @Override
    public Result queryById(Long id) {
        if (localCacheEnabled) {
            Shop shopLocal = shopLocalCache.getIfPresent(id);
            if (shopLocal != null) {
                log.trace("shop local cache hit, id={}", id);
                return Result.ok(shopLocal);
            }
            log.trace("shop local cache miss, id={}", id);
        }

        Shop shop = cacheClient.queryWithLogicalExpire(
                CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        if (localCacheEnabled) {
            shopLocalCache.put(id, shop);
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空！");
        }
        updateById(shop);
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        if (localCacheEnabled) {
            shopLocalCache.invalidate(id);
        }
        return Result.ok();
    }
    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        if (current == null || current < 1) {
            current = 1;
        }
        if (x == null || y == null) {
            List<Shop> shops = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(
                            current, SystemConstants.DEFAULT_PAGE_SIZE))
                    .getRecords();
            return Result.ok(shops);
        }

        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = SHOP_GEO_KEY + typeId;

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().radius(
                key,
                new Circle(new Point(x, y), new Distance(5000)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .limit(end)
        );
        if (results == null) {
            return Result.ok(new ArrayList<>());
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            return Result.ok(new ArrayList<>());
        }

        List<Long> ids = new ArrayList<>(list.size() - from);
        Map<String, Distance> distanceMap = list.stream()
                .skip(from)
                .peek(result -> ids.add(Long.valueOf(result.getContent().getName())))
                .collect(Collectors.toMap(
                        result -> result.getContent().getName(),
                        GeoResult::getDistance
                ));

        String idStr = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        List<Shop> shops = query()
                .in("id", ids)
                .last("ORDER BY FIELD(id," + idStr + ")")
                .list();
        shops.forEach(shop -> {
            Distance distance = distanceMap.get(shop.getId().toString());
            if (distance != null) {
                shop.setDistance(distance.getValue());
            }
        });
        return Result.ok(shops);
    }
}
