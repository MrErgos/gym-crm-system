package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Training creation request")
public record CreateTrainingRequest(

        @NotBlank(message = "Trainee username must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String traineeUsername,

        @NotBlank(message = "Trainer username must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String trainerUsername,

        @NotBlank(message = "Training name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Morning Strength Session")
        String trainingName,

        @NotNull(message = "Training date must not be null")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08-01")
        LocalDate trainingDate,

        @NotNull(message = "Training duration must not be null")
        @Positive(message = "Training duration must be a positive number of minutes")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        Integer trainingDuration
) {
}