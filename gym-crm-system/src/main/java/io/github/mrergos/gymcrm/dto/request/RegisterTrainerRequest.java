package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Trainer registration request")
public record RegisterTrainerRequest(

        @NotBlank(message = "First name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Anna")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Lee")
        String lastName,

        @NotNull(message = "Specialization (training type id) must not be null")
        @Schema(description = "Id of the training type this trainer specializes in",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        Long specializationId
) {
}
