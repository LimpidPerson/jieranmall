package com.ygnn.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 1、@EnableScheduling 开启定时任务
 * 2、@Scheduled 开启一个定时任务
 *
 * @author FangKun
 */
@Slf4j
@Component
//@EnableAsync
//@EnableScheduling
public class HelloSchedule {

    // @Scheduled(cron = "* * * * * ?")
    public void hello() {
        log.error("hello.....");
    }

}
