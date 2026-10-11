package com.hospitalvidasalud.notification_service.listener;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.hospitalvidasalud.notification_service.config.RabbitConfig;
import com.hospitalvidasalud.notification_service.model.Notification;
import com.hospitalvidasalud.notification_service.repository.NotificationRepository;

@Component
public class CitaConfirmadaListener {

    private static final Logger log = LoggerFactory.getLogger(CitaConfirmadaListener.class);

    private final NotificationRepository notificationRepository;

    public CitaConfirmadaListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onCitaConfirmada(Message message) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Evento CitaConfirmada recibido: {}", payload);

        Notification notification = new Notification();
        notification.setEventType("CitaConfirmada");
        notification.setMessage("Su cita ha sido confirmada");
        notification.setPayload(payload);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification)
                .subscribe(
                        saved -> log.info("Notificacion registrada con id {}", saved.getId()),
                        error -> log.error("No se pudo registrar la notificacion", error));
    }
}