package com.hospitalvidasalud.audit_service.listener;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.hospitalvidasalud.audit_service.config.RabbitConfig;
import com.hospitalvidasalud.audit_service.model.AuditEvent;
import com.hospitalvidasalud.audit_service.repository.AuditEventRepository;

@Component
public class CitaConfirmadaAuditListener {

    private static final Logger log = LoggerFactory.getLogger(CitaConfirmadaAuditListener.class);

    private final AuditEventRepository auditEventRepository;

    public CitaConfirmadaAuditListener(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onCitaConfirmada(Message message) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Auditoria recibio evento: {}", payload);

        AuditEvent audit = new AuditEvent();
        audit.setEventType("CitaConfirmada");
        audit.setResourceType("CITA");
        String id = extract(payload, "citaId");
        audit.setResourceId(id == null ? null : Long.valueOf(id));
        audit.setPreviousStatus(extract(payload, "estadoAnterior"));
        audit.setNewStatus(extract(payload, "estadoNuevo"));
        audit.setSource("appointment-service");
        audit.setCreatedAt(LocalDateTime.now());

        auditEventRepository.save(audit)
                .subscribe(
                        saved -> log.info("Evento de auditoria guardado con id {}", saved.getId()),
                        error -> log.error("No se pudo guardar la auditoria", error));
    }

    private static String extract(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\":\"?([^\",}]+)").matcher(json);
        return m.find() ? m.group(1) : null;
    }
}