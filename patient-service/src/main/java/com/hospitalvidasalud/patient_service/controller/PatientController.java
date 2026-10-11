
package com.hospitalvidasalud.patient_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.hospitalvidasalud.patient_service.model.Patient;
import com.hospitalvidasalud.patient_service.service.PatientService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Flux<Patient> findAll() {
        return patientService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Patient> findById(@PathVariable Long id) {
        return patientService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Patient> create(@RequestBody Patient patient) {
        return patientService.create(patient);
    }

    @PutMapping("/{id}")
    public Mono<Patient> update(
            @PathVariable Long id,
            @RequestBody Patient patient) {
        return patientService.update(id, patient);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return patientService.delete(id);
    }
}