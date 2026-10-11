package com.hospitalvidasalud.notification_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.hospitalvidasalud.notification_service.model.Notification;

public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {
}