package com.hospitalvidasalud.appointment_service.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.hospitalvidasalud.appointment_service.exception.AppointmentNotFoundException;
import com.hospitalvidasalud.appointment_service.exception.GlobalExceptionHandler;
import com.hospitalvidasalud.appointment_service.model.Appointment;
import com.hospitalvidasalud.appointment_service.model.AppointmentStatus;
import com.hospitalvidasalud.appointment_service.service.AppointmentService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AppointmentControllerTest {

    private AppointmentService service;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        service = mock(AppointmentService.class);
        client = WebTestClient
                .bindToController(new AppointmentController(service))
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarDevuelve200() {
        when(service.findAll()).thenReturn(Flux.empty());

        client.get().uri("/api/appointments")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void citaInexistenteDevuelve404() {
        when(service.findById(999L)).thenReturn(Mono.error(new AppointmentNotFoundException(999L)));

        client.get().uri("/api/appointments/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void transicionInvalidaDevuelve400() {
        when(service.cambiarEstado(1L, AppointmentStatus.PENDIENTE))
                .thenReturn(Mono.error(new IllegalArgumentException("Transicion no permitida: CONFIRMADA -> PENDIENTE")));

        client.patch().uri("/api/appointments/1/status")
                .bodyValue("{\"status\":\"PENDIENTE\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void crearCitaDevuelve201() {
        Appointment creada = new Appointment();
        creada.setId(1L);
        creada.setStatus("PENDIENTE");
        when(service.create(org.mockito.ArgumentMatchers.any(Appointment.class)))
                .thenReturn(Mono.just(creada));

        client.post().uri("/api/appointments")
                .header("Content-Type", "application/json")
                .bodyValue("{\"patientId\":2,\"specialty\":\"Cardiologia\",\"appointmentDate\":\"2026-10-20T10:00:00\"}")
                .exchange()
                .expectStatus().isCreated();
    }
}