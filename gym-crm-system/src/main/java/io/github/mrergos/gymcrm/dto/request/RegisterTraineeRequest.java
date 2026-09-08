package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Trainee registration request")
public record RegisterTraineeRequest(

        @NotBlank(message = "First name must not be blank")
        @Schema(description = "Trainee's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Schema(description = "Trainee's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @Past(message = "Date of birth must be in the past")
        @Schema(description = "Trainee's date of birth (optional)", example = "1995-05-20")
        LocalDate dateOfBirth,

        @Schema(description = "Trainee's address (optional)", example = "123 Main St")
        String address
) {
}
