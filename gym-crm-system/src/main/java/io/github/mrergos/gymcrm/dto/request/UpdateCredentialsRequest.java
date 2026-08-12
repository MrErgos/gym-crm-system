package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "New password request")
public record UpdateCredentialsRequest (
        @NotBlank(message = "New password must not be blank")
        @Size(min = 10, message = "New password must be at least 10 characters long")
        @Schema(description = "New password to set", example = "3%N3wCoolStrongPass!*")
        String newPassword) {
}
