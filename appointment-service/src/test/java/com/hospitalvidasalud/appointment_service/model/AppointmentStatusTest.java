package com.hospitalvidasalud.appointment_service.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppointmentStatusTest {

    @Test
    void pendientePuedeConfirmarseOCancelarse() {
        assertTrue(AppointmentStatus.PENDIENTE.puedeTransicionarA(AppointmentStatus.CONFIRMADA));
        assertTrue(AppointmentStatus.PENDIENTE.puedeTransicionarA(AppointmentStatus.CANCELADA));
    }

    @Test
    void confirmadaNoPuedeVolverAPendiente() {
        assertFalse(AppointmentStatus.CONFIRMADA.puedeTransicionarA(AppointmentStatus.PENDIENTE));
    }

    @Test
    void citaCanceladaNoPuedeConfirmarse() {
        assertFalse(AppointmentStatus.CANCELADA.puedeTransicionarA(AppointmentStatus.CONFIRMADA));
    }

    @Test
    void citaAtendidaEsEstadoFinal() {
        for (AppointmentStatus destino : AppointmentStatus.values()) {
            assertFalse(AppointmentStatus.ATENDIDA.puedeTransicionarA(destino));
        }
    }
}