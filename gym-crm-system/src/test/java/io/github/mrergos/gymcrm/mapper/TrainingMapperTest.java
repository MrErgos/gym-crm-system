package io.github.mrergos.gymcrm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import io.github.mrergos.gymcrm.dto.response.TrainingResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TrainingMapper tests")
class TrainingMapperTest {

    private final TrainingMapper mapper = new TrainingMapper();

    @Test
    @DisplayName("toResponse: maps training with all fields")
    void toResponse_shouldMapAllFields() {
        //given
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");

        Trainer trainer = new Trainer();
        trainer.setUsername("Anna.Lee");

        Training training = new Training();
        training.setId(1L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Strength Session");
        training.setTrainingType(new TrainingType(1L, "Strength"));
        training.setTrainingDate(LocalDate.of(2026, 8, 1));
        training.setTrainingDuration(60);

        //when
        TrainingResponse response = mapper.toResponse(training);

        //then
        assertEquals(1L, response.id());
        assertEquals("John.Doe", response.traineeUsername());
        assertEquals("Anna.Lee", response.trainerUsername());
        assertEquals("Morning Strength Session", response.trainingName());
        assertEquals("Strength", response.trainingTypeName());
        assertEquals(LocalDate.of(2026, 8, 1), response.trainingDate());
        assertEquals(60, response.trainingDuration());
    }
}