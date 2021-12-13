package com.ygnn.gulimall.order;

import com.ygnn.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void sendMessage(){
        OrderEntity orderEntity = new OrderEntity();
        //1、发送消息
        String msg = "Hello MLT";
        for (int i = 0; i < 10; i++) {
            orderEntity.setReceiverName("MLT" + i);
            orderEntity.setCreateTime(new Date());
            // rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", msg);
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity);
            log.info("Message Send Success: {}", msg);
        }
    }

    @Test
    void createExchange(){
        /**
         * DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         */
        amqpAdmin.declareExchange(new DirectExchange("hello-java-exchange", true, false));
        log.info("Exchange Create Success");
    }

    @Test
    void createQueue(){
        /**
         * Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
         */
        amqpAdmin.declareQueue(new Queue("hello-java-queue", true, false, false));
        log.info("Queue Create Success");
    }

    @Test
    void createBindingRelation(){
        /**
         * Binding(String destination, [目的地]
         * DestinationType destinationType, [目的地类型: 队列还是其他交换机]
         * String exchange, [交换机]
         * String routingKey, [路由键]
         * @Nullable Map<String, Object> arguments) [自定义参数]
         */
        amqpAdmin.declareBinding(new Binding("hello-java-queue", Binding.DestinationType.QUEUE, "hello-java-exchange", "hello.java", null));
        log.info("Binding Relation Success");
    }

}
