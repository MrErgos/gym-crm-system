package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.integration.dto.ActionType;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkingHoursResilientClient tests")
class WorkingHoursResilientClientTest {

    @Mock
    private WorkingHoursClient workingHoursClient;

    @InjectMocks
    private WorkingHoursResilientClient resilientClient;

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest(
                "John.Doe", "John", "Doe", true,
                LocalDate.of(2026, 8, 1), 60, ActionType.ADD);
    }

    @Test
    @DisplayName("submitWorkload: invokes WorkingHoursClient with given arguments")
    void submitWorkload_valid_shouldInvokeClient() throws Exception {
        //given
        String authHeader = "Bearer token";
        String txId = "tx-1";
        TrainerWorkloadRequest request = buildRequest();

        doNothing().when(workingHoursClient).submitWorkload(authHeader, txId, request);

        //when
        CompletableFuture<Void> future = resilientClient.submitWorkload(authHeader, txId, request);
        future.get();

        //then
        verify(workingHoursClient).submitWorkload(authHeader, txId, request);
    }

    @Test
    @DisplayName("getWorkload: invokes WorkingHoursClient and returns response")
    void getWorkload_valid_shouldReturnResponseFromClient() throws Exception {
        //given
        String authHeader = "Bearer token";
        String txId = "tx-1";
        String username = "John.Doe";
        TrainerWorkloadSummaryResponse expected = new TrainerWorkloadSummaryResponse(
                username, "John", "Doe", true, List.of());

        when(workingHoursClient.getWorkload(authHeader, txId, username)).thenReturn(expected);

        //when
        CompletableFuture<TrainerWorkloadSummaryResponse> future =
                resilientClient.getWorkload(authHeader, txId, username);

        //then
        assertEquals(expected, future.get());
        verify(workingHoursClient).getWorkload(authHeader, txId, username);
    }

    @Test
    @DisplayName("submitWorkload: propagates exception through CompletableFuture when client fails")
    void submitWorkload_clientThrows_shouldCompleteExceptionally() {
        //given
        String authHeader = "Bearer token";
        String txId = "tx-1";
        TrainerWorkloadRequest request = buildRequest();
        RuntimeException cause = new RuntimeException("downstream failure");

        doThrow(cause).when(workingHoursClient).submitWorkload(authHeader, txId, request);

        //when
        CompletableFuture<Void> future = resilientClient.submitWorkload(authHeader, txId, request);

        //then
        ExecutionException ex = assertThrows(ExecutionException.class, future::get);
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("getWorkload: propagates exception through CompletableFuture when client fails")
    void getWorkload_clientThrows_shouldCompleteExceptionally() {
        //given
        String authHeader = "Bearer token";
        String txId = "tx-1";
        String username = "John.Doe";
        RuntimeException cause = new RuntimeException("downstream failure");

        when(workingHoursClient.getWorkload(authHeader, txId, username)).thenThrow(cause);

        //when
        CompletableFuture<TrainerWorkloadSummaryResponse> future =
                resilientClient.getWorkload(authHeader, txId, username);

        //then
        ExecutionException ex = assertThrows(ExecutionException.class, future::get);
        assertEquals(cause, ex.getCause());
    }
}