package io.github.mrergos.gymcrm.integration;

import feign.FeignException;
import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.exception.ServiceUnavailableException;
import io.github.mrergos.gymcrm.integration.dto.ActionType;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkingHoursGateway tests")
class WorkingHoursGatewayTest {

    @Mock
    private WorkingHoursResilientClient resilientClient;

    @Mock
    private ServiceTokenProvider serviceTokenProvider;

    @InjectMocks
    private WorkingHoursGateway gateway;

    private Trainer buildTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Jane.Smith");
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setActive(true);
        trainer.setSpecialization(new TrainingType("yoga"));
        return trainer;
    }

    private Training buildTraining(Trainer trainer) {
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(new TrainingType("yoga"));
        training.setTrainingDate(LocalDate.of(2026, 8, 1));
        training.setTrainingDuration(60);
        return training;
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("notify: submits mapped workload request to resilient client")
    void notify_valid_shouldSubmitMappedRequest() {
        //given
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.submitWorkload(anyString(), anyString(), any(TrainerWorkloadRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        //when
        gateway.notify(training, ActionType.ADD);

        //then
        ArgumentCaptor<TrainerWorkloadRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(resilientClient).submitWorkload(eq("Bearer token"), anyString(), captor.capture());

        TrainerWorkloadRequest request = captor.getValue();
        assertEquals("Jane.Smith", request.trainerUsername());
        assertEquals("Jane", request.trainerFirstName());
        assertEquals("Smith", request.trainerLastName());
        assertEquals(Boolean.TRUE, request.isActive());
        assertEquals(LocalDate.of(2026, 8, 1), request.trainingDate());
        assertEquals(60, request.trainingDuration());
        assertEquals(ActionType.ADD, request.actionType());
    }

    @Test
    @DisplayName("notify: reuses existing transaction id from MDC")
    void notify_withExistingMdcTransactionId_shouldReuseIt() {
        //given
        MDC.put("transactionId", "existing-tx-id");
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.submitWorkload(anyString(), anyString(), any(TrainerWorkloadRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        //when
        gateway.notify(training, ActionType.ADD);

        //then
        verify(resilientClient).submitWorkload(eq("Bearer token"), eq("existing-tx-id"), any());
    }

    @Test
    @DisplayName("notify: generates new transaction id when MDC is empty")
    void notify_withoutMdcTransactionId_shouldGenerateNewOne() {
        //given
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.submitWorkload(anyString(), anyString(), any(TrainerWorkloadRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        //when
        gateway.notify(training, ActionType.ADD);

        //then
        ArgumentCaptor<String> txIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(resilientClient).submitWorkload(eq("Bearer token"), txIdCaptor.capture(), any());
        assertDoesNotThrow(() -> UUID.fromString(txIdCaptor.getValue()));
    }

    @Test
    @DisplayName("notify: does not throw when submitWorkload future completes exceptionally")
    void notify_futureFailsAsynchronously_shouldNotThrow() {
        //given
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);
        CompletableFuture<Void> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("downstream error"));

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.submitWorkload(anyString(), anyString(), any(TrainerWorkloadRequest.class)))
                .thenReturn(failedFuture);

        //when
        //then
        assertDoesNotThrow(() -> gateway.notify(training, ActionType.ADD));
    }

    @Test
    @DisplayName("notify: catches and swallows synchronous exception from resilient client")
    void notify_clientThrowsSynchronously_shouldNotThrow() {
        //given
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.submitWorkload(anyString(), anyString(), any(TrainerWorkloadRequest.class)))
                .thenThrow(new RuntimeException("unexpected failure"));

        //when
        //then
        assertDoesNotThrow(() -> gateway.notify(training, ActionType.ADD));
    }

    @Test
    @DisplayName("getWorkloadSummary: returns response when resilient client succeeds")
    void getWorkloadSummary_valid_shouldReturnResponse() throws Exception {
        //given
        String username = "Jane.Smith";
        TrainerWorkloadSummaryResponse expected = new TrainerWorkloadSummaryResponse(
                username, "Jane", "Smith", true, List.of());

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.getWorkload(anyString(), anyString(), eq(username)))
                .thenReturn(CompletableFuture.completedFuture(expected));

        //when
        TrainerWorkloadSummaryResponse result = gateway.getWorkloadSummary(username);

        //then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("getWorkloadSummary: throws EntityNotFoundException on FeignException.NotFound")
    void getWorkloadSummary_feignNotFound_shouldThrowEntityNotFoundException() {
        //given
        String username = "Unknown.User";
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        CompletableFuture<TrainerWorkloadSummaryResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(notFound);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.getWorkload(anyString(), anyString(), eq(username)))
                .thenReturn(failedFuture);

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> gateway.getWorkloadSummary(username));
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException when circuit breaker open")
    void getWorkloadSummary_circuitOpen_shouldThrowServiceUnavailableException() {
        //given
        String username = "Jane.Smith";
        CallNotPermittedException circuitOpen = mock(CallNotPermittedException.class);
        CompletableFuture<TrainerWorkloadSummaryResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(circuitOpen);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.getWorkload(anyString(), anyString(), eq(username)))
                .thenReturn(failedFuture);

        //when
        //then
        assertThrows(ServiceUnavailableException.class, () -> gateway.getWorkloadSummary(username));
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException on timeout")
    void getWorkloadSummary_timeout_shouldThrowServiceUnavailableException() {
        //given
        String username = "Jane.Smith";
        CompletableFuture<TrainerWorkloadSummaryResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new TimeoutException("timed out"));

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.getWorkload(anyString(), anyString(), eq(username)))
                .thenReturn(failedFuture);

        //when
        //then
        assertThrows(ServiceUnavailableException.class, () -> gateway.getWorkloadSummary(username));
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException for unknown failure")
    void getWorkloadSummary_unknownFailure_shouldThrowServiceUnavailableException() {
        //given
        String username = "Jane.Smith";
        RuntimeException cause = new RuntimeException("unexpected");
        CompletableFuture<TrainerWorkloadSummaryResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(cause);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.getWorkload(anyString(), anyString(), eq(username)))
                .thenReturn(failedFuture);

        //when
        //then
        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class,
                () -> gateway.getWorkloadSummary(username));
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException and preserves interrupt status")
    void getWorkloadSummary_interrupted_shouldThrowServiceUnavailableExceptionAndPreserveInterruptFlag() {
        //given
        String username = "Jane.Smith";

        @SuppressWarnings("unchecked")
        CompletableFuture<TrainerWorkloadSummaryResponse> interruptingFuture = mock(CompletableFuture.class);

        when(serviceTokenProvider.bearerToken()).thenReturn("Bearer token");
        when(resilientClient.getWorkload(anyString(), anyString(), eq(username)))
                .thenReturn(interruptingFuture);
        try {
            when(interruptingFuture.get()).thenThrow(new InterruptedException("interrupted"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        //when
        //then
        try {
            assertThrows(ServiceUnavailableException.class, () -> gateway.getWorkloadSummary(username));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}