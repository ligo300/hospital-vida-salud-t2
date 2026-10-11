package com.hospitalvidasalud.appointment_service.client;

public record PatientDto(Long id, String firstName, String lastName,
                         String documentNumber, String email, String phone) {
}