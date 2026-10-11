package com.hospitalvidasalud.patient_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.hospitalvidasalud.patient_service.exception.GlobalExceptionHandler;
import com.hospitalvidasalud.patient_service.exception.PatientNotFoundException;
import com.hospitalvidasalud.patient_service.model.Patient;
import com.hospitalvidasalud.patient_service.service.PatientService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PatientControllerTest {

    private PatientService service;
    private WebTestClient client;

    private static final String JSON = "{\"firstName\":\"Juan\",\"lastName\":\"Perez\","
            + "\"documentNumber\":\"12345678\",\"email\":\"juan.perez@mail.com\",\"phone\":\"987654321\"}";

    @BeforeEach
    void setUp() {
        service = mock(PatientService.class);
        client = WebTestClient
                .bindToController(new PatientController(service))
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarDevuelve200() {
        when(service.findAll()).thenReturn(Flux.empty());

        client.get().uri("/api/patients")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registrarPacienteDevuelve201() {
        Patient creado = new Patient();
        creado.setId(1L);
        creado.setFirstName("Juan");
        when(service.create(any(Patient.class))).thenReturn(Mono.just(creado));

        client.post().uri("/api/patients")
                .header("Content-Type", "application/json")
                .bodyValue(JSON)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void pacienteDuplicadoDevuelve400() {
        when(service.create(any(Patient.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Ya existe un paciente con ese documento")));

        client.post().uri("/api/patients")
                .header("Content-Type", "application/json")
                .bodyValue(JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void pacienteInexistenteDevuelve404() {
        when(service.findById(999L)).thenReturn(Mono.error(new PatientNotFoundException(999L)));

        client.get().uri("/api/patients/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void eliminarDevuelve204() {
        when(service.delete(1L)).thenReturn(Mono.empty());

        client.delete().uri("/api/patients/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}