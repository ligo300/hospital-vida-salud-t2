package com.hospitalvidasalud.appointment_service.model;

import java.util.Map;
import java.util.Set;

public enum AppointmentStatus {
    PENDIENTE,
    CONFIRMADA,
    ATENDIDA,
    CANCELADA,
    AUSENTE;

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> TRANSICIONES = Map.of(
            PENDIENTE,   Set.of(CONFIRMADA, CANCELADA),
            CONFIRMADA,  Set.of(ATENDIDA, CANCELADA, AUSENTE),
            ATENDIDA,    Set.of(),
            CANCELADA,   Set.of(PENDIENTE),   // reprogramación
            AUSENTE,     Set.of(PENDIENTE)    // reprogramación
    );

    public boolean puedeTransicionarA(AppointmentStatus nuevo) {
        return TRANSICIONES.getOrDefault(this, Set.of()).contains(nuevo);
    }
}