package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Trainee profile update request")
public record UpdateTraineeRequest(

        @NotBlank(message = "First name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        String address,

        @NotNull(message = "isActive must be provided")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isActive
) {
}