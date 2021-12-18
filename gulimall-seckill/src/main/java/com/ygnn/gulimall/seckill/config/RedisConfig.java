package com.ygnn.gulimall.seckill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisConfig {

    @Autowired
    public static StringRedisTemplate redisTemplate;

    public static void main(String[] args) {
        redisTemplate.opsForValue().set("李先亨", "傻逼");
        String lsh = redisTemplate.opsForValue().get("李先亨");
        System.out.println(lsh);
    }
}
