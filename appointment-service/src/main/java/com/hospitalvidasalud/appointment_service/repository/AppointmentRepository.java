package com.hospitalvidasalud.appointment_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.hospitalvidasalud.appointment_service.model.Appointment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AppointmentRepository
        extends ReactiveCrudRepository<Appointment, Long> {

    Mono<Appointment> findByCode(String code);

    Flux<Appointment> findByPatientId(Long patientId);

    Mono<Boolean> existsByPatientIdAndSpecialtyAndAppointmentDate(
            Long patientId, String specialty, java.time.LocalDateTime appointmentDate);
}