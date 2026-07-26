package io.github.mrergos.gymcrm.mapper;

import io.github.mrergos.gymcrm.dto.response.TraineeShortResponse;
import io.github.mrergos.gymcrm.dto.response.TrainerResponse;
import io.github.mrergos.gymcrm.dto.response.TrainingTypeResponse;
import io.github.mrergos.gymcrm.entity.Trainer;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    public TrainerResponse toResponse(Trainer trainer) {
        return new TrainerResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                new TrainingTypeResponse(
                        trainer.getSpecialization().getTrainingTypeName(),
                        trainer.getSpecialization().getId()),
                trainer.getTrainees().stream()
                        .map(trainee -> new TraineeShortResponse(
                                trainee.getUsername(), trainee.getFirstName(), trainee.getLastName()))
                        .toList()
        );
    }
}