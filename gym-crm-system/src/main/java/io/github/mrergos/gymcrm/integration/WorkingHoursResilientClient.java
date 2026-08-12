package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
class WorkingHoursResilientClient {

    private final WorkingHoursClient workingHoursClient;

    WorkingHoursResilientClient(WorkingHoursClient workingHoursClient) {
        this.workingHoursClient = workingHoursClient;
    }

    @CircuitBreaker(name = "workingHoursService", fallbackMethod = "fallback")
    @TimeLimiter(name = "workingHoursService")
    CompletableFuture<Void> submitWorkload(String authorizationHeader, String transactionId,
                                           TrainerWorkloadRequest request) {
        return CompletableFuture.runAsync(() ->
                workingHoursClient.submitWorkload(authorizationHeader, transactionId, request));
    }

    private CompletableFuture<Void> fallback(String authorizationHeader, String transactionId,
                                             TrainerWorkloadRequest request, Throwable throwable) {
        return CompletableFuture.failedFuture(throwable);
    }

    @CircuitBreaker(name = "workingHoursService", fallbackMethod = "fallbackGetWorkload")
    @TimeLimiter(name = "workingHoursService")
    CompletableFuture<TrainerWorkloadSummaryResponse> getWorkload(String authorizationHeader, String transactionId,
                                                                  String username) {
        return CompletableFuture.supplyAsync(() ->
                workingHoursClient.getWorkload(authorizationHeader, transactionId, username));
    }

    private CompletableFuture<TrainerWorkloadSummaryResponse> fallbackGetWorkload(
            String authorizationHeader, String transactionId, String username, Throwable throwable) {
        return CompletableFuture.failedFuture(throwable);
    }
}