package io.github.mrergos.gymcrm.dto.response;

import java.time.LocalDate;
import java.util.List;

public record UpdateTraineeResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerShortResponse> trainersList
) {
}
