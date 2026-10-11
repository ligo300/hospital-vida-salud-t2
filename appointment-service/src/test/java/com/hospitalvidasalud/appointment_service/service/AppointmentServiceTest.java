package com.hospitalvidasalud.appointment_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.hospitalvidasalud.appointment_service.client.PatientDto;
import com.hospitalvidasalud.appointment_service.client.PatientServiceClient;
import com.hospitalvidasalud.appointment_service.config.RabbitConfig;
import com.hospitalvidasalud.appointment_service.exception.AppointmentNotFoundException;
import com.hospitalvidasalud.appointment_service.model.Appointment;
import com.hospitalvidasalud.appointment_service.model.AppointmentStatus;
import com.hospitalvidasalud.appointment_service.repository.AppointmentRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AppointmentServiceTest {

    private AppointmentRepository repository;
    private PatientServiceClient patientClient;
    private RabbitTemplate rabbitTemplate;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        repository = mock(AppointmentRepository.class);
        patientClient = mock(PatientServiceClient.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        service = new AppointmentService(repository, patientClient, rabbitTemplate);
    }

    private Appointment nuevaCita() {
        Appointment a = new Appointment();
        a.setPatientId(2L);
        a.setSpecialty("Cardiologia");
        a.setAppointmentDate(LocalDateTime.of(2026, 10, 20, 10, 0));
        a.setProfessionalCode("DR-001");
        a.setReason("Control administrativo");
        return a;
    }

    private Appointment citaGuardada(AppointmentStatus estado) {
        Appointment a = nuevaCita();
        a.setId(1L);
        a.setCode("CITA-TEST0001");
        a.setStatus(estado.name());
        return a;
    }

    @Test
    void creaCitaPendienteCuandoElPacienteExisteYNoHayDuplicado() {
        when(patientClient.findById(2L)).thenReturn(
                Mono.just(new PatientDto(2L, "Juan", "Perez", "12345678", "j@mail.com", "987654321")));
        when(repository.existsByPatientIdAndSpecialtyAndAppointmentDate(anyLong(), anyString(), any()))
                .thenReturn(Mono.just(false));
        when(repository.save(any(Appointment.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(nuevaCita()))
                .expectNextMatches(c -> "PENDIENTE".equals(c.getStatus())
                        && c.getCode() != null && c.getCode().startsWith("CITA-"))
                .verifyComplete();
    }

    @Test
    void rechazaCitaDuplicada() {
        when(patientClient.findById(2L)).thenReturn(
                Mono.just(new PatientDto(2L, "Juan", "Perez", "12345678", "j@mail.com", "987654321")));
        when(repository.existsByPatientIdAndSpecialtyAndAppointmentDate(anyLong(), anyString(), any()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.create(nuevaCita()))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Ya existe una cita"))
                .verify();

        verify(repository, never()).save(any(Appointment.class));
    }

    @Test
    void rechazaCitaSiElPacienteNoExiste() {
        when(patientClient.findById(999L)).thenReturn(
                Mono.error(new IllegalArgumentException("El paciente con id 999 no existe")));

        Appointment cita = nuevaCita();
        cita.setPatientId(999L);

        StepVerifier.create(service.create(cita))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("no existe"))
                .verify();

        verify(repository, never()).save(any(Appointment.class));
    }

    @Test
    void citaInexistenteLanzaNotFound() {
        when(repository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(999L))
                .expectError(AppointmentNotFoundException.class)
                .verify();
    }

    @Test
    void rechazaTransicionNoPermitida() {
        when(repository.findById(1L))
                .thenReturn(Mono.just(citaGuardada(AppointmentStatus.CONFIRMADA)));

        StepVerifier.create(service.cambiarEstado(1L, AppointmentStatus.PENDIENTE))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Transicion no permitida"))
                .verify();

        verify(repository, never()).save(any(Appointment.class));
    }

    @Test
    void confirmarCitaPublicaElEvento() {
        when(repository.findById(1L))
                .thenReturn(Mono.just(citaGuardada(AppointmentStatus.PENDIENTE)));
        when(repository.save(any(Appointment.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.cambiarEstado(1L, AppointmentStatus.CONFIRMADA))
                .expectNextMatches(c -> "CONFIRMADA".equals(c.getStatus()))
                .verifyComplete();

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.ROUTING_CITA_CONFIRMADA),
                any(Object.class));
    }

    @Test
    void cancelarCitaNoPublicaEvento() {
        when(repository.findById(1L))
                .thenReturn(Mono.just(citaGuardada(AppointmentStatus.PENDIENTE)));
        when(repository.save(any(Appointment.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.cambiarEstado(1L, AppointmentStatus.CANCELADA))
                .expectNextCount(1)
                .verifyComplete();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}