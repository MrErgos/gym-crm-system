package io.github.mrergos.workinghours.dto.request;

import io.github.mrergos.workinghours.entity.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Trainer workload event: a training session that was planned (ADD) or cancelled (DELETE)")
public record TrainerWorkloadRequest(

        @NotBlank(message = "Trainer username must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "John.Doe")
        String trainerUsername,

        @NotBlank(message = "Trainer first name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "John")
        String trainerFirstName,

        @NotBlank(message = "Trainer last name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Doe")
        String trainerLastName,

        @NotNull(message = "isActive must be provided")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isActive,

        @NotNull(message = "Training date must not be null")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08-01")
        LocalDate trainingDate,

        @NotNull(message = "Training duration must not be null")
        @Positive(message = "Training duration must be a positive number of minutes")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        Integer trainingDuration,

        @NotNull(message = "Action type must not be null")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "ADD")
        ActionType actionType
) {
}
