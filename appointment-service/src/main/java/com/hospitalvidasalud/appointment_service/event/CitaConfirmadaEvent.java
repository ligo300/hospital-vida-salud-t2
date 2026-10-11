package com.hospitalvidasalud.appointment_service.event;

import java.time.LocalDateTime;

public record CitaConfirmadaEvent(
        Long citaId,
        String code,
        Long pacienteId,
        String especialidad,
        LocalDateTime fechaHora,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime occurredAt
) {
}