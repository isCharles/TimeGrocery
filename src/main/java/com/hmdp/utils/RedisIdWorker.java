package com.hmdp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
public class RedisIdWorker {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final long BEGIN_TIMESTAMP = 1767225600L;
    private static final int COUNT_BITS = 32;

    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.toEpochSecond(ZoneOffset.UTC);
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date, 1);
        return (timestamp - BEGIN_TIMESTAMP) << COUNT_BITS | count;
    }
/*    public static void main(String[] args) {
        //输出2026年1月1日0点0分0秒的时间戳(不是当前)
        long timestamp = LocalDateTime.of(2026, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
        System.out.println(timestamp);
    }*/

}
