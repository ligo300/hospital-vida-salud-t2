
package com.hospitalvidasalud.patient_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.hospitalvidasalud.patient_service.model.Patient;

import reactor.core.publisher.Mono;

public interface PatientRepository
        extends ReactiveCrudRepository<Patient, Long> {

    Mono<Patient> findByDocumentNumber(String documentNumber);
}