package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Training response")
public record TrainingResponse(
        Long id,
        String traineeUsername,
        String trainerUsername,
        String trainingName,
        String trainingTypeName,
        LocalDate trainingDate,
        Integer trainingDuration
) {
}