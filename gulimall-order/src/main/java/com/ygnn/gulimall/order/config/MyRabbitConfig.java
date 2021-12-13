package com.ygnn.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author FangKun
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 使用JSON序列化机制，进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就回调
     *      1、spring.rabbitmq.publisher-confirms=true
     *      2、设置确认回调ConfirmCallback
     * 2、消息正确抵达队列进行回调
     *      1、spring.rabbitmq.publisher-returns=true
     *         spring.rabbitmq.template.mandatory=true
     *      2、设置确认回调ReturnCallback
     * 3、消费端确认(保证每个消息都被正确消费,此时才可以Broker删除这个消息)
     *      spring.rabbitmq.listener.simple.acknowledge-mode=manual 手动签收
     *      1、默认是自动确认的，只要消息接收到，客户端会自动确认服务端就会移除这个消息
     *          问题：
     *              我们收到很多消息，自动回复给服务器ack，如果只有一个消息处理成功然后宕机了就会发生消息丢失的问题
     *              消费者手动确认模式。只要我们没有明确告诉MQ货物被签收就没有Ack消息就一直是unacked昨天。即使Consumer宕机消息也不会丢失，会重新变为Ready
     *      2、如何签收：
     *          channel.basicAck(deliveryTag,false);签收：业务成功完成就应该签收
     *          channel.basicNack(deliveryTag,false,true);拒签：业务失败，拒签
     * @PostConstruct: 对象创建完成以后，执行这个方法
     */
    @PostConstruct
    public void initRabbiTemplate(){
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

            /**
             * 成功回调此方法,只要消息抵达Broker就ack=true
             * @param correlationData 当前消息的唯一关联数据(这个是信息的唯一ID)
             * @param b 成功还是失败
             * @param s 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                /**
                 * 防止消息丢失
                 * 1、做好消息确认机制(publisher, consumer【手动ack】)
                 * 2、每一个发送的消息都在数据库做好记录，定期将失败的消息再次发送一遍
                 */
                // 服务器收到了
                System.out.println("confirm...correlationData[" + correlationData + "] ==> ack[" + b + "] ==> cause[" + s + "]");
            }
        });

        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message 投递失败的详细信息
             * @param i 回复的状态码
             * @param s 回复的文本内容
             * @param s1 当时这个消息发给哪个交换机
             * @param s2 当时这个消息用的哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                // 报错误了。修改数据库当前消息的状态 ===》 错误
                System.out.println("Fail...Message[" + message + "] ==> replyCode[" + i + "] ==> replyText[" + s + "] ==> exchange[" + s1 + "] ==> routingKey[" + s2 + "]");
            }
        });

    }

}
