package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Monthly breakdown of training duration for a single calendar year")
public record YearSummaryResponse(

        @Schema(description = "Calendar year", example = "2026")
        int year,

        @Schema(description = "Per-month summaries within this year")
        List<MonthSummaryResponse> months
) {
}
