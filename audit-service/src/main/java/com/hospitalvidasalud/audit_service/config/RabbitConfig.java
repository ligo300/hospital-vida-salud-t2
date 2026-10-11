package com.hospitalvidasalud.audit_service.config;

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
    public static final String QUEUE = "auditoria.cita-confirmada";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue auditoriaQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding auditoriaBinding(Queue auditoriaQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(auditoriaQueue).to(hospitalExchange).with(ROUTING_KEY);
    }
}