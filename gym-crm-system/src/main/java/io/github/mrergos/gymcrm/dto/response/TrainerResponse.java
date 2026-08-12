package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Trainer profile response")
public record TrainerResponse(
        String username,
        String firstName,
        String lastName,
        boolean isActive,
        TrainingTypeResponse specialization,
        List<TraineeShortResponse> traineesList
) {
}