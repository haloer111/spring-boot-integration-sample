package com.gexiao.sample.producer;

import com.gexiao.sample.entity.Order;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 消息确认的回调，需要在配置文件种设置spring.rabbitmq.publisher-confirm-type: correlated
     */
    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        System.out.println("correlationData = " + correlationData);
        System.out.println("ack = " + ack);
        if (!ack) {
            System.out.println("异常处理。。。");
        }
    };

    /**
     * 消息发送后的回调，需要在配置文件种设置spring.rabbitmq.template.mandatory: true
     */
    final RabbitTemplate.ReturnCallback returnCallback = (message, replyCode, replyText, exchange, routingKey) -> {
        System.out.println("exchange = " + exchange);
        System.out.println("routingKey = " + routingKey);
        System.out.println("replyCode = " + replyCode);
        System.out.println("replyText = " + replyText);
    };

    /**
     * 发送消息
     * @param message
     * @param properties
     * @throws Exception
     */
    public void send(Object message, Map<String, Object> properties) throws Exception {
        MessageHeaders messageHeaders = new MessageHeaders(properties);
        MessageBuilder.createMessage(message, messageHeaders);
        CorrelationData correlationData = new CorrelationData();
        // 全局唯一
        correlationData.setId("123456789");
        rabbitTemplate.convertAndSend("exchange1", "springboot.add", message,correlationData);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
    }

    public void send(Order order) throws Exception {
        CorrelationData correlationData = new CorrelationData();
        // 全局唯一
        correlationData.setId("09876521");
        rabbitTemplate.convertAndSend("exchange-2", "springboot2.add", order,correlationData);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
    }
}
