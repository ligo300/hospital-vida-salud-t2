package com.hospitalvidasalud.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "hospital-events";
    public static final String ROUTING_KEY = "cita.confirmada";
    public static final String QUEUE = "notificaciones.cita-confirmada";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificacionesQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding notificacionesBinding(Queue notificacionesQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(hospitalExchange).with(ROUTING_KEY);
    }
}