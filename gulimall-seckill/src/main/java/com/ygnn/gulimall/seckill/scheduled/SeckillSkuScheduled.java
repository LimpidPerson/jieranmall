package com.ygnn.gulimall.seckill.scheduled;

import com.ygnn.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 * 每天晚上3点上架最近三天需要秒杀的商品
 *
 * @author FangKun
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    private final String UPLOAD_LOCK = "seckill:upload:lock";
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private RedissonClient redissonClient;

    //TODO 幂等性处理
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //1、重复上架无需处理
        // 分布式锁.锁住的业务执行完成，状态已经更新完成。释放锁以后其他人获取到就会拿到最新的状态。
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }

}
