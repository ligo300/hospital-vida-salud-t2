package com.hospitalvidasalud.audit_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.hospitalvidasalud.audit_service.model.AuditEvent;

public interface AuditEventRepository extends ReactiveCrudRepository<AuditEvent, Long> {
}