package com.hospitalvidasalud.patient_service.exception;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(Long id) {
        super("Paciente no encontrado con id: " + id);
    }
}