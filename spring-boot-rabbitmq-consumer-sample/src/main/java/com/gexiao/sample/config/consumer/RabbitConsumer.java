package com.gexiao.sample.config.consumer;

import com.gexiao.sample.entity.Order;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitConsumer {

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "queue-1", durable = "true"),
                    exchange = @Exchange(value = "exchange-1", type = "topic", ignoreDeclarationExceptions = "true"),
                    key = "springboot1.*"
            )
    )
    @RabbitHandler
    public void onMessage(Message message, Channel channel) throws Exception {
        System.out.println("message = " + message.getPayload());
        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        // 手动确认ack
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.listener.order.queue.name}", durable = "${spring.rabbitmq.listener.order.queue.durable}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.listener.order.exchange.name}", type = "${spring.rabbitmq.listener.order.exchange.type}", ignoreDeclarationExceptions = "${spring.rabbitmq.listener.order.exchange.ignoreDeclarationExceptions}"),
                    key = "${spring.rabbitmq.listener.order.key}"
            )
    )
    @RabbitHandler
    public void onOrderMessage(@Payload Order order, Channel channel, @Headers Map<String, Object> properties) throws Exception {
        System.out.println("message = " + order.toString());
        Long deliveryTag = (Long) properties.get(AmqpHeaders.DELIVERY_TAG);
        // 手动确认ack
        channel.basicAck(deliveryTag, false);
    }
}
