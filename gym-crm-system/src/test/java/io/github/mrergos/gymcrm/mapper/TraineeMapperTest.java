package io.github.mrergos.gymcrm.mapper;

import io.github.mrergos.gymcrm.dto.request.RegisterTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.mrergos.gymcrm.dto.response.TraineeResponse;
import io.github.mrergos.gymcrm.dto.response.UpdateTraineeResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TraineeMapper tests")
class TraineeMapperTest {

    private final TraineeMapper mapper = new TraineeMapper();

    private Trainer buildTrainer(Long id, String username, String type) {
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setUsername(username);
        trainer.setFirstName("Anna");
        trainer.setLastName("Lee");
        trainer.setSpecialization(new TrainingType(1L, type));
        return trainer;
    }

    @Test
    @DisplayName("toEntity: RegisterTraineeRequest maps all fields")
    void toEntity_fromRegisterRequest_shouldMapFields() {
        //given
        RegisterTraineeRequest request = new RegisterTraineeRequest(
                "John", "Doe", LocalDate.of(1995, 5, 20), "123 Main St");

        //when
        Trainee trainee = mapper.toEntity(request);

        //then
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        assertEquals(LocalDate.of(1995, 5, 20), trainee.getDateOfBirth());
        assertEquals("123 Main St", trainee.getAddress());
    }

    @Test
    @DisplayName("toEntity: UpdateTraineeRequest maps all fields including active flag")
    void toEntity_fromUpdateRequest_shouldMapFields() {
        //given
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "John", "Doe", LocalDate.of(1995, 5, 20), "123 Main St", false);

        //when
        Trainee trainee = mapper.toEntity(request);

        //then
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        assertEquals(LocalDate.of(1995, 5, 20), trainee.getDateOfBirth());
        assertEquals("123 Main St", trainee.getAddress());
        assertFalseActive(trainee);
    }

    private void assertFalseActive(Trainee trainee) {
        assertEquals(false, trainee.isActive());
    }

    @Test
    @DisplayName("toResponse: maps trainee with trainers list")
    void toResponse_withTrainers_shouldMapAllFields() {
        //given
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setDateOfBirth(LocalDate.of(1995, 5, 20));
        trainee.setAddress("123 Main St");
        trainee.setActive(true);
        trainee.setTrainers(Set.of(buildTrainer(1L, "Anna.Lee", "Cardio")));

        //when
        TraineeResponse response = mapper.toResponse(trainee);

        //then
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(LocalDate.of(1995, 5, 20), response.dateOfBirth());
        assertEquals("123 Main St", response.address());
        assertTrue(response.isActive());
        assertEquals(1, response.trainersList().size());
        assertEquals("Anna.Lee", response.trainersList().get(0).username());
        assertEquals("Cardio", response.trainersList().get(0).trainingTypeResponse().trainingTypeName());
    }

    @Test
    @DisplayName("toResponse: maps trainee with empty trainers list")
    void toResponse_noTrainers_shouldReturnEmptyList() {
        //given
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setActive(true);
        trainee.setTrainers(Set.of());

        //when
        TraineeResponse response = mapper.toResponse(trainee);

        //then
        assertTrue(response.trainersList().isEmpty());
    }

    @Test
    @DisplayName("toUpdateResponse: maps trainee with username and trainers list")
    void toUpdateResponse_withTrainers_shouldMapAllFields() {
        //given
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setDateOfBirth(LocalDate.of(1995, 5, 20));
        trainee.setAddress("123 Main St");
        trainee.setActive(true);
        trainee.setTrainers(Set.of(buildTrainer(1L, "Anna.Lee", "Cardio")));

        //when
        UpdateTraineeResponse response = mapper.toUpdateResponse(trainee);

        //then
        assertEquals("John.Doe", response.username());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(LocalDate.of(1995, 5, 20), response.dateOfBirth());
        assertEquals("123 Main St", response.address());
        assertTrue(response.isActive());
        assertEquals(1, response.trainersList().size());
    }

    @Test
    @DisplayName("toUpdateResponse: maps trainee with empty trainers list")
    void toUpdateResponse_noTrainers_shouldReturnEmptyList() {
        //given
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setActive(true);
        trainee.setTrainers(Set.of());

        //when
        UpdateTraineeResponse response = mapper.toUpdateResponse(trainee);

        //then
        assertTrue(response.trainersList().isEmpty());
    }
}