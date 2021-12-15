package com.ygnn.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FangKun
 */
@Configuration
public class MyRedissonConfig {

    @Bean
    public RedissonClient redisson() {
        //1、创建配置
        Config config = new Config();
        //2、可以用"rediss://"来启用 SSL 连接
        config.useSingleServer().setAddress("redis://192.168.134.156:6379");
        //3、根据Config创建出RedissonClient实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }

}
