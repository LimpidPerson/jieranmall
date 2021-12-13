package com.ygnn.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.gulimall.order.dao.OrderItemDao;
import com.ygnn.gulimall.order.entity.OrderEntity;
import com.ygnn.gulimall.order.entity.OrderItemEntity;
import com.ygnn.gulimall.order.service.OrderItemService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    @RabbitListener(queues = {"hello-java-queue"})
    public void receiveMessage(Message message, OrderEntity context, Channel channel){
        // 输出发送的信息
        System.out.println("接收到信息: " + message + " ===> 内容: " + context);
        // Channel内按顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("DeliveryTag() = " + deliveryTag);
        try {
            // 签收货物/消息
            if (deliveryTag % 2 == 0){
                // 收货
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物: " + deliveryTag);
            } else {
                // 退货 requeue=false 丢弃 requeue=true 发回服务器，服务器重新入队
                channel.basicNack(deliveryTag, false, false);
                // channel.basicReject(deliveryTag, false);
                System.out.println("没有签收货物: " + deliveryTag);
            }
        } catch (Exception e){
            // 网络中断等异常情况
            e.getMessage();
        }
    }
























}