package com.hospitalvidasalud.appointment_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.hospitalvidasalud.appointment_service.client.PatientServiceClient;
import com.hospitalvidasalud.appointment_service.config.RabbitConfig;
import com.hospitalvidasalud.appointment_service.event.CitaConfirmadaEvent;
import com.hospitalvidasalud.appointment_service.exception.*;
import com.hospitalvidasalud.appointment_service.model.Appointment;
import com.hospitalvidasalud.appointment_service.model.AppointmentStatus;
import com.hospitalvidasalud.appointment_service.repository.AppointmentRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientServiceClient patientServiceClient;
    private final RabbitTemplate rabbitTemplate;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientServiceClient patientServiceClient,
                              RabbitTemplate rabbitTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.patientServiceClient = patientServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Flux<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Mono<Appointment> findById(Long id) {
        return appointmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(id)));
    }

    public Flux<Appointment> findByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public Mono<Appointment> create(Appointment appointment) {
        // 1. Comunicación síncrona: el paciente DEBE existir
        return patientServiceClient.findById(appointment.getPatientId())
                // 2. Validar cita duplicada (mismo paciente + especialidad + fecha/hora)
                .flatMap(patient -> appointmentRepository
                        .existsByPatientIdAndSpecialtyAndAppointmentDate(
                                appointment.getPatientId(),
                                appointment.getSpecialty(),
                                appointment.getAppointmentDate())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.<Appointment>error(new IllegalArgumentException(
                                        "Ya existe una cita igual para este paciente"));
                            }
                            appointment.setCode("CITA-" + UUID.randomUUID()
                                    .toString().substring(0, 8).toUpperCase());
                            appointment.setStatus(AppointmentStatus.PENDIENTE.name());
                            appointment.setCreatedAt(LocalDateTime.now());
                            appointment.setUpdatedAt(LocalDateTime.now());
                            return appointmentRepository.save(appointment);
                        }));
    }

    public Mono<Appointment> cambiarEstado(Long id, AppointmentStatus nuevoEstado) {
        return appointmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(id)))
                .flatMap(appointment -> {
                    AppointmentStatus actual = AppointmentStatus
                            .valueOf(appointment.getStatus());

                    if (!actual.puedeTransicionarA(nuevoEstado)) {
                        return Mono.error(new IllegalArgumentException(
                                "Transicion no permitida: " + actual + " -> " + nuevoEstado));
                    }

                    String estadoAnterior = appointment.getStatus();
                    appointment.setStatus(nuevoEstado.name());
                    appointment.setUpdatedAt(LocalDateTime.now());

                    return appointmentRepository.save(appointment)
                            .doOnNext(saved -> {
                                // 3. Publicar evento cuando se confirma la cita
                                if (nuevoEstado == AppointmentStatus.CONFIRMADA) {
                                    rabbitTemplate.convertAndSend(
                                            RabbitConfig.EXCHANGE,
                                            RabbitConfig.ROUTING_CITA_CONFIRMADA,
                                            new CitaConfirmadaEvent(
                                                    saved.getId(),
                                                    saved.getCode(),
                                                    saved.getPatientId(),
                                                    saved.getSpecialty(),
                                                    saved.getAppointmentDate(),
                                                    estadoAnterior,
                                                    saved.getStatus(),
                                                    LocalDateTime.now()));
                                }
                            });
                });
    }
}