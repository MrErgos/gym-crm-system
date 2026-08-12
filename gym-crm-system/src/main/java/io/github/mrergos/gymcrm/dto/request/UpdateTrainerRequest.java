package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Trainer profile update request")
public record UpdateTrainerRequest(

        @NotBlank(message = "First name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @NotNull(message = "Specialization (training type id) must not be null")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long specializationId,

        @NotNull(message = "isActive must be provided")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isActive
) {
}