package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short trainee representation, used inside a trainer's profile")
public record TraineeShortResponse(
        String username,
        String firstName,
        String lastName
) {
}