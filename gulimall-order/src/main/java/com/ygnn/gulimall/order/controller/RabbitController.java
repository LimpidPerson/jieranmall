package com.ygnn.gulimall.order.controller;

import com.ygnn.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendMQ")
    public String sendMQ(Integer num){
        OrderEntity orderEntity = new OrderEntity();
        //1、发送消息
        for (int i = 0; i < 10; i++) {
            orderEntity.setReceiverName("MLT" + i);
            orderEntity.setCreateTime(new Date());
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity, new CorrelationData(UUID.randomUUID().toString()));
            log.info("Message Send Success");
        }
        return "ok";
    }

}
