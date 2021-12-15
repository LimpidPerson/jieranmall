package com.ygnn.gulimall.order.listener;

import com.ygnn.common.to.mq.SeckillOrderTo;
import com.ygnn.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.channels.Channel;

@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo orderTo, Channel channel, Message message) {
        log.debug("准备创建秒杀单的详细信息......");
        orderService.createSeckillOrder(orderTo);
    }

}
