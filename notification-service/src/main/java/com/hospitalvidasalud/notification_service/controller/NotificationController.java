package com.hospitalvidasalud.notification_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalvidasalud.notification_service.model.Notification;
import com.hospitalvidasalud.notification_service.repository.NotificationRepository;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public Flux<Notification> findAll() {
        return notificationRepository.findAll();
    }
}