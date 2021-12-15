package com.ygnn.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ygnn.common.to.mq.SeckillOrderTo;
import com.ygnn.common.utils.R;
import com.ygnn.common.vo.MemberRespVo;
import com.ygnn.gulimall.seckill.feign.CouponFeignService;
import com.ygnn.gulimall.seckill.feign.ProductFeignService;
import com.ygnn.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.ygnn.gulimall.seckill.service.SeckillService;
import com.ygnn.gulimall.seckill.to.SeckillSkuRedisTo;
import com.ygnn.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.ygnn.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.StringUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHA_PREFIX = "seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、扫描最近三天需要参与秒杀活动的商品
        R r = couponFeignService.getLates3DaySession();
        if (r.getCode() == 0) {
            // 需要上架的商品
            List<SeckillSessionsWithSkus> data = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            System.out.println("data = " + data);
            // 缓存到Redis中
            //1、缓存活动信息
            saveSessionInfos(data);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(data);
        }
    }

    /**
     * 返回当前时间可以参与的秒杀商品信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于那个秒杀场次
        long time = System.currentTimeMillis();

        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            // seckill:sessions:1639479600000_1639481400000
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                //2、获取这个秒杀场次需要的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHA_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null && list.size() > 0) {
                    return list.stream().map(item -> JSON.parseObject((String) item, SeckillSkuRedisTo.class)).collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1、找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHA_PREFIX);

        Set<String> keys = hashOps.keys();
        for (String key : keys) {
            String regx = "\\d_" + skuId;
            if (Pattern.matches(regx, key)) {
                String json = hashOps.get(key);
                SeckillSkuRedisTo redis = JSON.parseObject(json, SeckillSkuRedisTo.class);

                // 随机码处理
                if (System.currentTimeMillis() >= redis.getStartTime() && System.currentTimeMillis() <= redis.getEndTime()) {
                } else {
                    redis.setRandomCode(null);
                }
                return redis;
            }
        }
        return null;
    }

    /**
     * 秒杀操作
     * TODO 上架秒杀商品的时候每一个数据都有过期时间。
     * TODO 此秒杀流程还不完善，简化了收货地址等信息。
     *
     * @param killId
     * @param key
     * @return
     */
    @Override
    public String kill(String killId, String key) {
        long l1 = System.currentTimeMillis();
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        //1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHA_PREFIX);
        String json = hashOps.get(killId);
        if (!StringUtil.isEmpty(json)) {
            SeckillSkuRedisTo redis = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //1、校验时间的合法性
            if (System.currentTimeMillis() >= redis.getStartTime() && System.currentTimeMillis() <= redis.getEndTime()) {
                //2、校验随机码和商品id
                if (key.equals(redis.getRandomCode())) {
                    //3、验证购物数量是否合理，此处我规定只能唯一，所以为做判断
                    //4、验证这个人是否已经购买过。幂等性操作；如果秒杀成功就去占位。 userId_sessionId_skuId
                    String redisKey = respVo.getId() + "_" + redis.getSkuId();
                    // SETNX写值并设置自动过期时间
                    Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, killId, redis.getEndTime(), TimeUnit.MILLISECONDS);
                    if (aBoolean) {
                        // 占位成功说明从来没有买过,获取信号量
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redis.getRandomCode());
                        // 扣减信号量，扣减成功表示秒杀成功
                        if (semaphore.tryAcquire(1)) {
                            // 秒杀成功，快速下单，发送MQ信息  20ms左右
                            String timeId = IdWorker.getTimeId();
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setOrderSn(timeId);
                            orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                            orderTo.setSkuId(redis.getSkuId());
                            orderTo.setSeckillPrice(redis.getSeckillPrice());
                            orderTo.setMemberId(respVo.getId());
                            rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                            long l2 = System.currentTimeMillis();
                            log.error("耗时为：" + (l2 - l1));
                            return timeId;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                // 缓存活动信息
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            // 准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHA_PREFIX);
            session.getRelationSkus().stream().forEach(sku -> {
                //4、商品的随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!ops.hasKey(sku.getPromotionSessionId() + "_" + sku.getSkuId())) {
                    System.out.println("为什么又进来缓存信号量了");
                    // 缓存商品
                    SeckillSkuRedisTo redis = new SeckillSkuRedisTo();
                    //1、sku的基本数据
                    R r = productFeignService.getSkuInfo(sku.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redis.setSkuInfo(skuInfo);
                    }

                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(sku, redis);


                    //3、设置当前商品秒杀时间信息
                    redis.setStartTime(session.getStartTime().getTime());
                    redis.setEndTime(session.getEndTime().getTime());

                    redis.setRandomCode(token);

                    String s = JSON.toJSONString(redis);
                    ops.put(sku.getPromotionSessionId() + "_" + sku.getSkuId(), s);

                    //5、使用库存作为分布式的信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(sku.getSeckillCount());
                }
            });
        });
    }

}