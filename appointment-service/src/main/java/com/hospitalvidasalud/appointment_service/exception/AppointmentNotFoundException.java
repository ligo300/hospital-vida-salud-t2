package com.hospitalvidasalud.appointment_service.exception;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long id) {
        super("Cita no encontrada con id: " + id);
    }
}