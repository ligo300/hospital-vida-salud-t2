package com.hospitalvidasalud.audit_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalvidasalud.audit_service.model.AuditEvent;
import com.hospitalvidasalud.audit_service.repository.AuditEventRepository;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping
    public Flux<AuditEvent> findAll() {
        return auditEventRepository.findAll();
    }
}