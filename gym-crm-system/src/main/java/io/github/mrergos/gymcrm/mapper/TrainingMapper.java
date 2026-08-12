package io.github.mrergos.gymcrm.mapper;

import io.github.mrergos.gymcrm.dto.response.TrainingResponse;
import io.github.mrergos.gymcrm.entity.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public TrainingResponse toResponse(Training training) {
        return new TrainingResponse(
                training.getId(),
                training.getTrainee().getUsername(),
                training.getTrainer().getUsername(),
                training.getTrainingName(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDate(),
                training.getTrainingDuration()
        );
    }
}