package io.github.mrergos.gymcrm.controller;

import io.github.mrergos.gymcrm.dto.request.RegisterTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeTrainersRequest;
import io.github.mrergos.gymcrm.dto.response.CredentialsResponse;
import io.github.mrergos.gymcrm.dto.response.TraineeResponse;
import io.github.mrergos.gymcrm.dto.response.TrainerShortResponse;
import io.github.mrergos.gymcrm.dto.response.UpdateTraineeResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.mapper.TraineeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeController tests")
class TraineeControllerTest {

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private GymFacade facade;

    @InjectMocks
    private TraineeController controller;

    private Trainer buildTrainer(String username) {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setFirstName("Anna");
        trainer.setLastName("Lee");
        trainer.setSpecialization(new TrainingType(1L, "Cardio"));
        return trainer;
    }

    @Test
    @DisplayName("registerTrainee: maps request, delegates to facade and returns credentials")
    void registerTrainee_validRequest_shouldReturnCredentials() {
        //given
        RegisterTraineeRequest registerRequest = new RegisterTraineeRequest("John", "Doe", LocalDate.of(1995, 5, 20), "123 Main St");
        Trainee mapped = new Trainee();
        Trainee saved = new Trainee();
        saved.setUsername("John.Doe");
        saved.setPassword("generatedPass");

        when(traineeMapper.toEntity(registerRequest)).thenReturn(mapped);
        when(facade.createTraineeProfile(mapped)).thenReturn(saved);

        //when
        CredentialsResponse response = controller.registerTrainee(registerRequest);

        //then
        assertEquals("John.Doe", response.username());
        assertEquals("generatedPass", response.password());
    }

    @Test
    @DisplayName("getTrainee: returns mapped trainee")
    void getTrainee_existingUsername_shouldReturnResponse() {
        //given
        Trainee trainee = new Trainee();
        TraineeResponse mappedResponse = new TraineeResponse("John", "Doe", null, null, true, List.of());

        when(facade.getTraineeProfile("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(mappedResponse);

        //when
        TraineeResponse response = controller.getTrainee("John.Doe");

        //then
        assertEquals(mappedResponse, response);
    }

    @Test
    @DisplayName("getTrainee: missing trainee throws EntityNotFoundException")
    void getTrainee_missingUsername_shouldThrow() {
        //given
        when(facade.getTraineeProfile("unknown")).thenReturn(Optional.empty());

        //when //then
        assertThrows(EntityNotFoundException.class, () -> controller.getTrainee("unknown"));
    }

    @Test
    @DisplayName("updateTrainee: maps request, sets username and delegates to facade")
    void updateTrainee_validRequest_shouldReturnUpdatedResponse() {
        //given
        UpdateTraineeRequest updateRequest = new UpdateTraineeRequest("John", "Doe", LocalDate.of(1995, 5, 20), "123 Main St", true);
        Trainee mapped = new Trainee();
        Trainee updated = new Trainee();
        UpdateTraineeResponse mappedResponse = new UpdateTraineeResponse("John.Doe", "John", "Doe", null, null, true, List.of());

        when(traineeMapper.toEntity(updateRequest)).thenReturn(mapped);
        when(facade.updateTraineeProfile(mapped)).thenReturn(updated);
        when(traineeMapper.toUpdateResponse(updated)).thenReturn(mappedResponse);

        //when
        UpdateTraineeResponse response = controller.updateTrainee("John.Doe", updateRequest);

        //then
        assertEquals("John.Doe", mapped.getUsername());
        assertEquals(mappedResponse, response);
    }

    @Test
    @DisplayName("toggleTraineeStatus: toggles via facade")
    void toggleTraineeStatus_shouldReturnOk() {
        //when
        ResponseEntity<Void> response = controller.toggleTraineeStatus("John.Doe");

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(facade).toggleTraineeActive("John.Doe");
    }

    @Test
    @DisplayName("updateTraineeTrainers: maps trainers into short responses")
    void updateTraineeTrainers_validRequest_shouldReturnMappedList() {
        //given
        UpdateTraineeTrainersRequest body = new UpdateTraineeTrainersRequest(List.of("Anna.Lee"));
        Trainer trainer = buildTrainer("Anna.Lee");

        when(facade.updateTraineeTrainers("John.Doe", List.of("Anna.Lee")))
                .thenReturn(List.of(trainer));

        //when
        List<TrainerShortResponse> response = controller.updateTraineeTrainers("John.Doe", body);

        //then
        assertEquals(1, response.size());
        assertEquals("Anna.Lee", response.get(0).username());
        assertEquals("Cardio", response.get(0).trainingTypeResponse().trainingTypeName());
    }

    @Test
    @DisplayName("getAvailableTrainers: maps trainers not assigned into short responses")
    void getAvailableTrainers_shouldReturnMappedList() {
        //given
        Trainer trainer = buildTrainer("Anna.Lee");

        when(facade.getTrainersNotAssigned("John.Doe")).thenReturn(List.of(trainer));

        //when
        List<TrainerShortResponse> response = controller.getAvailableTrainers("John.Doe");

        //then
        assertEquals(1, response.size());
        assertEquals("Anna.Lee", response.get(0).username());
    }

    @Test
    @DisplayName("getAvailableTrainers: returns empty list when none available")
    void getAvailableTrainers_noneAvailable_shouldReturnEmptyList() {
        //given
        when(facade.getTrainersNotAssigned("John.Doe")).thenReturn(List.of());

        //when
        List<TrainerShortResponse> response = controller.getAvailableTrainers("John.Doe");

        //then
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("deleteTrainee: deletes via facade")
    void deleteTrainee_shouldDelegateToFacade() {
        //when
        controller.deleteTrainee("John.Doe");

        //then
        verify(facade).deleteTraineeProfile("John.Doe");
    }
}