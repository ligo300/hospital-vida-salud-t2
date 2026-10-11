
package com.hospitalvidasalud.patient_service.service;

import org.springframework.stereotype.Service;

import com.hospitalvidasalud.patient_service.exception.PatientNotFoundException;
import com.hospitalvidasalud.patient_service.model.Patient;
import com.hospitalvidasalud.patient_service.repository.PatientRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Flux<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Mono<Patient> findById(Long id) {
        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(new PatientNotFoundException(id)));
    }

    public Mono<Patient> create(Patient patient) {
        return patientRepository
                .findByDocumentNumber(patient.getDocumentNumber())
                .flatMap(existing -> Mono.<Patient>error(
                        new IllegalArgumentException(
                                "Ya existe un paciente con ese documento")))
                .switchIfEmpty(Mono.defer(() -> patientRepository.save(patient)));
    }

    public Mono<Patient> update(Long id, Patient patient) {
        return patientRepository.findById(id)
        		.switchIfEmpty(Mono.error(new PatientNotFoundException(id)))
                .flatMap(existing ->
                        patientRepository.findByDocumentNumber(
                                patient.getDocumentNumber())
                        .filter(other -> !other.getId().equals(id))
                        .flatMap(other -> Mono.<Patient>error(
                                new IllegalArgumentException(
                                        "El documento ya pertenece a otro paciente")))
                        .switchIfEmpty(Mono.defer(() -> {
                            existing.setFirstName(patient.getFirstName());
                            existing.setLastName(patient.getLastName());
                            existing.setDocumentNumber(patient.getDocumentNumber());
                            existing.setEmail(patient.getEmail());
                            existing.setPhone(patient.getPhone());
                            return patientRepository.save(existing);
                        })));
    }

    public Mono<Void> delete(Long id) {
        return patientRepository.deleteById(id);
    }
}