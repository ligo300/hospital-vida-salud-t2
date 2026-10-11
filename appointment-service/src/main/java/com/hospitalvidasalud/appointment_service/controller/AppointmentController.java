package com.hospitalvidasalud.appointment_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.hospitalvidasalud.appointment_service.model.Appointment;
import com.hospitalvidasalud.appointment_service.model.AppointmentStatus;
import com.hospitalvidasalud.appointment_service.service.AppointmentService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public Flux<Appointment> findAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Appointment> findById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @GetMapping("/patient/{patientId}")
    public Flux<Appointment> findByPatientId(@PathVariable Long patientId) {
        return appointmentService.findByPatientId(patientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Appointment> create(@RequestBody Appointment appointment) {
        return appointmentService.create(appointment);
    }

    @PatchMapping("/{id}/status")
    public Mono<Appointment> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        AppointmentStatus nuevo = AppointmentStatus.valueOf(body.get("status"));
        return appointmentService.cambiarEstado(id, nuevo);
    }
}