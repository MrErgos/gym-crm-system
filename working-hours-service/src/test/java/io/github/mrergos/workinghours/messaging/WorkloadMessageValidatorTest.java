package io.github.mrergos.workinghours.messaging;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.entity.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadMessageValidator tests")
class WorkloadMessageValidatorTest {

    private final WorkloadMessageValidator validator = new WorkloadMessageValidator();

    private TrainerWorkloadRequest buildValidRequest() {
        return new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2026, 8, 1), 60, ActionType.ADD);
    }

    @Test
    @DisplayName("validate: returns no errors for a fully populated request")
    void validate_fullyPopulatedRequest_shouldReturnNoErrors() {
        //given
        //when
        List<String> errors = validator.validate(buildValidRequest());

        //then
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("validate: returns a single error when the request itself is null")
    void validate_nullRequest_shouldReturnSinglePayloadError() {
        //given
        //when
        List<String> errors = validator.validate(null);

        //then
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("payload"));
    }

    @Test
    @DisplayName("validate: reports missing trainerUsername when blank")
    void validate_blankTrainerUsername_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "  ", "Jane", "Smith", true, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainerUsername")));
    }

    @Test
    @DisplayName("validate: reports missing trainerUsername when null")
    void validate_nullTrainerUsername_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                null, "Jane", "Smith", true, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainerUsername")));
    }

    @Test
    @DisplayName("validate: reports missing trainerFirstName when blank")
    void validate_blankTrainerFirstName_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "", "Smith", true, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainerFirstName")));
    }

    @Test
    @DisplayName("validate: reports missing trainerLastName when blank")
    void validate_blankTrainerLastName_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", " ", true, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainerLastName")));
    }

    @Test
    @DisplayName("validate: reports missing isActive when null")
    void validate_nullIsActive_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", null, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("isActive")));
    }

    @Test
    @DisplayName("validate: reports missing trainingDate when null")
    void validate_nullTrainingDate_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", true, null, 60, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainingDate")));
    }

    @Test
    @DisplayName("validate: reports missing trainingDuration when null")
    void validate_nullTrainingDuration_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", true, LocalDate.of(2026, 8, 1), null, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainingDuration")));
    }

    @Test
    @DisplayName("validate: reports non-positive trainingDuration")
    void validate_nonPositiveTrainingDuration_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", true, LocalDate.of(2026, 8, 1), 0, ActionType.ADD);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("trainingDuration") && e.contains("positive")));
    }

    @Test
    @DisplayName("validate: reports missing actionType when null")
    void validate_nullActionType_shouldReportError() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", true, LocalDate.of(2026, 8, 1), 60, null);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertTrue(errors.stream().anyMatch(e -> e.contains("actionType")));
    }

    @Test
    @DisplayName("validate: accumulates all errors when multiple required fields are missing at once")
    void validate_multipleMissingFields_shouldReportAllErrors() {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                null, null, null, null, null, null, null);

        //when
        List<String> errors = validator.validate(request);

        //then
        assertEquals(7, errors.size());
    }
}
