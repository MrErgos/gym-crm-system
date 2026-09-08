package io.github.mrergos.workinghours.controller;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadController")
class TrainerWorkloadControllerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadController controller;

    @Test
    @DisplayName("submitWorkload delegates the request to the service layer unchanged")
    void submitWorkloadDelegatesToService() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "John.Doe", "John", "Doe", true, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        controller.submitWorkload(request);

        verify(trainerWorkloadService).applyWorkload(request);
    }

    @Test
    @DisplayName("getWorkloadSummary delegates to the service and returns its response")
    void getWorkloadSummaryDelegatesAndReturnsResponse() {
        TrainerWorkloadSummaryResponse expected = new TrainerWorkloadSummaryResponse(
                "John.Doe", "John", "Doe", true, Collections.emptyList());
        when(trainerWorkloadService.getSummary("John.Doe")).thenReturn(expected);

        TrainerWorkloadSummaryResponse actual = controller.getWorkloadSummary("John.Doe");

        assertThat(actual).isEqualTo(expected);
        verify(trainerWorkloadService).getSummary(eq("John.Doe"));
    }
}
