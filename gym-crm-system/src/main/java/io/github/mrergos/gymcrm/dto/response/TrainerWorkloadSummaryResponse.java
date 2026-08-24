package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A trainer's full training-hours summary, grouped by year and month, " +
        "as reported by the working-hours-service")
public record TrainerWorkloadSummaryResponse(

        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,

        @Schema(description = "Whether the trainer is currently active")
        boolean trainerStatus,

        @Schema(description = "Years for which this trainer has recorded trainings")
        List<YearSummaryResponse> years
) {
}
