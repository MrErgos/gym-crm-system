package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.exception.ServiceUnavailableException;
import io.github.mrergos.gymcrm.integration.dto.ActionType;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
public class WorkingHoursGateway {

    private static final Logger log = LoggerFactory.getLogger(WorkingHoursGateway.class);
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    private final WorkingHoursResilientClient resilientClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public WorkingHoursGateway(WorkingHoursResilientClient resilientClient,
                               ServiceTokenProvider serviceTokenProvider) {
        this.resilientClient = resilientClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    public void notify(Training training, ActionType actionType) {
        String transactionId = currentOrNewTransactionId();
        try {
            TrainerWorkloadRequest request = toRequest(training, actionType);

            log.info("Submitting workload event to working-hours-service: trainer={}, action={}, txId={}",
                    request.trainerUsername(), actionType, transactionId);

            resilientClient.submitWorkload(serviceTokenProvider.bearerToken(), transactionId, request)
                    .whenComplete((result, throwable) -> {
                        if (throwable == null) {
                            log.info("Workload event accepted by working-hours-service: trainer={}, action={}, txId={}",
                                    request.trainerUsername(), actionType, transactionId);
                        } else {
                            logFailure(request.trainerUsername(), actionType, transactionId, unwrap(throwable));
                        }
                    });
        } catch (Exception e) {
            log.error("Unexpected error while notifying working-hours-service, skipping: " +
                    "action={}, txId={}, error={}", actionType, transactionId, e.toString());
        }
    }

    public TrainerWorkloadSummaryResponse getWorkloadSummary(String username) {
        String transactionId = currentOrNewTransactionId();
        log.info("Requesting workload summary from working-hours-service: trainer={}, txId={}",
                username, transactionId);

        try {
            TrainerWorkloadSummaryResponse response = resilientClient
                    .getWorkload(serviceTokenProvider.bearerToken(), transactionId, username)
                    .get();

            log.info("Workload summary received from working-hours-service: trainer={}, txId={}",
                    username, transactionId);
            return response;
        } catch (ExecutionException e) {
            throw mapWorkloadSummaryFailure(username, transactionId, unwrap(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException(
                    "Interrupted while waiting for working-hours-service response, trainer=" + username, e);
        }
    }

    private RuntimeException mapWorkloadSummaryFailure(String username, String transactionId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            log.warn("No workload data found for trainer in working-hours-service: trainer={}, txId={}",
                    username, transactionId);
            return new EntityNotFoundException("No workload data found for trainer: " + username);
        }
        if (throwable instanceof CallNotPermittedException) {
            log.warn("Circuit open for working-hours-service, cannot fetch workload summary: trainer={}, txId={}",
                    username, transactionId);
            return new ServiceUnavailableException(
                    "working-hours-service is currently unavailable, please try again later");
        }
        if (throwable instanceof TimeoutException) {
            log.warn("Timed out calling working-hours-service for workload summary: trainer={}, txId={}",
                    username, transactionId);
            return new ServiceUnavailableException(
                    "working-hours-service did not respond in time, please try again later");
        }
        log.error("Failed to fetch workload summary from working-hours-service: trainer={}, txId={}, error={}",
                username, transactionId, throwable.toString());
        return new ServiceUnavailableException(
                "working-hours-service is currently unavailable, please try again later", throwable);
    }

    private void logFailure(String trainerUsername, ActionType actionType, String transactionId, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            log.warn("Circuit open for working-hours-service, skipping workload notification: " +
                    "trainer={}, action={}, txId={}", trainerUsername, actionType, transactionId);
        } else if (throwable instanceof TimeoutException) {
            log.warn("Timed out calling working-hours-service, skipping workload notification: " +
                    "trainer={}, action={}, txId={}", trainerUsername, actionType, transactionId);
        } else {
            log.error("Failed to notify working-hours-service, skipping workload notification: " +
                            "trainer={}, action={}, txId={}, error={}",
                    trainerUsername, actionType, transactionId, throwable.toString());
        }
    }

    private Throwable unwrap(Throwable throwable) {
        return throwable.getCause() != null ? throwable.getCause() : throwable;
    }

    private TrainerWorkloadRequest toRequest(Training training, ActionType actionType) {
        var trainer = training.getTrainer();
        return new TrainerWorkloadRequest(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
    }

    private String currentOrNewTransactionId() {
        String existing = MDC.get(TRANSACTION_ID_MDC_KEY);
        return (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
    }
}