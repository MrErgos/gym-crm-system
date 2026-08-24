package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Total training duration for a single calendar month")
public record MonthSummaryResponse(

        @Schema(description = "Month number, 1-12", example = "8")
        int month,

        @Schema(description = "Total training duration for this month, in minutes", example = "180")
        int trainingSummaryDuration
) {
}
