package com.hospitalvidasalud.patient_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospitalvidasalud.patient_service.exception.PatientNotFoundException;
import com.hospitalvidasalud.patient_service.model.Patient;
import com.hospitalvidasalud.patient_service.repository.PatientRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PatientServiceTest {

    private PatientRepository repository;
    private PatientService service;

    @BeforeEach
    void setUp() {
        repository = mock(PatientRepository.class);
        service = new PatientService(repository);
    }

    private Patient paciente(Long id, String documento) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("Juan");
        p.setLastName("Perez");
        p.setDocumentNumber(documento);
        p.setEmail("juan.perez@mail.com");
        p.setPhone("987654321");
        return p;
    }

    @Test
    void registraPacienteValido() {
        when(repository.findByDocumentNumber("12345678")).thenReturn(Mono.empty());
        when(repository.save(any(Patient.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(paciente(null, "12345678")))
                .expectNextMatches(p -> "12345678".equals(p.getDocumentNumber()))
                .verifyComplete();

        verify(repository).save(any(Patient.class));
    }

    @Test
    void rechazaPacienteDuplicado() {
        when(repository.findByDocumentNumber("12345678"))
                .thenReturn(Mono.just(paciente(1L, "12345678")));

        StepVerifier.create(service.create(paciente(null, "12345678")))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Ya existe un paciente"))
                .verify();

        verify(repository, never()).save(any(Patient.class));
    }

    @Test
    void pacienteInexistenteLanzaNotFound() {
        when(repository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(999L))
                .expectError(PatientNotFoundException.class)
                .verify();
    }

    @Test
    void actualizaPacienteConDocumentoLibre() {
        when(repository.findById(1L)).thenReturn(Mono.just(paciente(1L, "12345678")));
        when(repository.findByDocumentNumber("12345678"))
                .thenReturn(Mono.just(paciente(1L, "12345678")));
        when(repository.save(any(Patient.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Patient cambios = paciente(null, "12345678");
        cambios.setFirstName("Juan Carlos");

        StepVerifier.create(service.update(1L, cambios))
                .expectNextMatches(p -> "Juan Carlos".equals(p.getFirstName()))
                .verifyComplete();
    }

    @Test
    void rechazaActualizarConDocumentoDeOtroPaciente() {
        when(repository.findById(1L)).thenReturn(Mono.just(paciente(1L, "12345678")));
        when(repository.findByDocumentNumber("87654321"))
                .thenReturn(Mono.just(paciente(2L, "87654321")));

        StepVerifier.create(service.update(1L, paciente(null, "87654321")))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("ya pertenece a otro paciente"))
                .verify();

        verify(repository, never()).save(any(Patient.class));
    }
}