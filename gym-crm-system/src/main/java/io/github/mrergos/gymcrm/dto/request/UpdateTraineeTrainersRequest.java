package io.github.mrergos.gymcrm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Full replacement list of trainer usernames to assign to a trainee")
public record UpdateTraineeTrainersRequest(

        @NotEmpty(message = "Trainer usernames list must not be empty")
        @Schema(description = "Usernames of the trainers to assign", example = "[\"John.Smith\", \"Anna.Lee\"]")
        List<String> trainerUsernames
) {
}