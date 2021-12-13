package com.ygnn.gulimall.order.web;

import com.ygnn.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author FangKun
 */
@Controller
public class HelloController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        //模拟订单下单成功
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setCreateTime(new Date());

        //给MQ发送信息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", entity);
        return "OK";
    }

    @GetMapping("{page}.html")
    public String listPage(@PathVariable("page") String page){
        System.out.println("页面跳转了没有鸭? --> " + page);
        return page;
    }

}
