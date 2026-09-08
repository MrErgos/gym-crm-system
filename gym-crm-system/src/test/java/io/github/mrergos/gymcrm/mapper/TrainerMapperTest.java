package io.github.mrergos.gymcrm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import io.github.mrergos.gymcrm.dto.response.TrainerResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TrainerMapper tests")
class TrainerMapperTest {

    private final TrainerMapper mapper = new TrainerMapper();

    private Trainee buildTrainee(Long id, String username) {
        Trainee trainee = new Trainee();
        trainee.setId(id);
        trainee.setUsername(username);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        return trainee;
    }

    @Test
    @DisplayName("toResponse: maps trainer with trainees list")
    void toResponse_withTrainees_shouldMapAllFields() {
        //given
        Trainer trainer = new Trainer();
        trainer.setUsername("Anna.Lee");
        trainer.setFirstName("Anna");
        trainer.setLastName("Lee");
        trainer.setActive(true);
        trainer.setSpecialization(new TrainingType(1L, "Cardio"));
        trainer.setTrainees(Set.of(buildTrainee(1L, "John.Doe")));

        //when
        TrainerResponse response = mapper.toResponse(trainer);

        //then
        assertEquals("Anna.Lee", response.username());
        assertEquals("Anna", response.firstName());
        assertEquals("Lee", response.lastName());
        assertTrue(response.isActive());
        assertEquals("Cardio", response.specialization().trainingTypeName());
        assertEquals(1L, response.specialization().id());
        assertEquals(1, response.traineesList().size());
        assertEquals("John.Doe", response.traineesList().get(0).username());
        assertEquals("John", response.traineesList().get(0).firstName());
        assertEquals("Doe", response.traineesList().get(0).lastName());
    }

    @Test
    @DisplayName("toResponse: maps trainer with empty trainees list")
    void toResponse_noTrainees_shouldReturnEmptyList() {
        //given
        Trainer trainer = new Trainer();
        trainer.setUsername("Anna.Lee");
        trainer.setFirstName("Anna");
        trainer.setLastName("Lee");
        trainer.setActive(false);
        trainer.setSpecialization(new TrainingType(2L, "Yoga"));
        trainer.setTrainees(Set.of());

        //when
        TrainerResponse response = mapper.toResponse(trainer);

        //then
        assertTrue(response.traineesList().isEmpty());
        assertEquals(false, response.isActive());
    }
}