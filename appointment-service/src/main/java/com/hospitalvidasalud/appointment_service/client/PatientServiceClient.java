package com.hospitalvidasalud.appointment_service.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class PatientServiceClient {

    private final WebClient.Builder webClientBuilder;

    public PatientServiceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<PatientDto> findById(Long id) {
        return webClientBuilder.build()
                .get()
                .uri("http://patient-service/api/patients/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(
                                new IllegalArgumentException(
                                        "El paciente con id " + id + " no existe")))
                .bodyToMono(PatientDto.class);
    }
}