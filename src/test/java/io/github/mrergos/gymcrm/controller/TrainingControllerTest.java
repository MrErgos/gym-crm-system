package io.github.mrergos.gymcrm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.github.mrergos.gymcrm.controller.TrainingController;
import io.github.mrergos.gymcrm.dto.request.CreateTrainingRequest;
import io.github.mrergos.gymcrm.dto.response.TrainingResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.facade.Credentials;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.mapper.TrainingMapper;
import io.github.mrergos.gymcrm.security.BasicAuthCredentialsResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingController tests")
class TrainingControllerTest {

    @Mock
    private GymFacade facade;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private BasicAuthCredentialsResolver credentialsResolver;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TrainingController controller;

    private final Credentials credentials = new Credentials("John.Doe", "password123");

    @Test
    @DisplayName("createTraining: resolves entities, delegates to facade and returns mapped response")
    void createTraining_validRequest_shouldReturnResponse() {
        //given
        CreateTrainingRequest createRequest = new CreateTrainingRequest(
                "John.Doe", "Anna.Lee", "Morning Strength Session", LocalDate.of(2026, 8, 1), 60);

        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");

        Trainer trainer = new Trainer();
        trainer.setUsername("Anna.Lee");
        trainer.setSpecialization(new TrainingType(1L, "Strength"));

        Training saved = new Training();
        saved.setId(1L);
        TrainingResponse mappedResponse = new TrainingResponse(1L, "John.Doe", "Anna.Lee",
                "Morning Strength Session", "Strength", LocalDate.of(2026, 8, 1), 60);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTraineeProfile(credentials, "John.Doe")).thenReturn(Optional.of(trainee));
        when(facade.getTrainerProfile(credentials, "Anna.Lee")).thenReturn(Optional.of(trainer));
        when(facade.createTraining(eq(credentials), any(Training.class))).thenReturn(saved);
        when(trainingMapper.toResponse(saved)).thenReturn(mappedResponse);

        //when
        TrainingResponse response = controller.createTraining(request, createRequest);

        //then
        assertEquals(mappedResponse, response);
    }

    @Test
    @DisplayName("createTraining: missing trainee throws EntityNotFoundException")
    void createTraining_missingTrainee_shouldThrow() {
        //given
        CreateTrainingRequest createRequest = new CreateTrainingRequest(
                "unknown", "Anna.Lee", "Morning Strength Session", LocalDate.of(2026, 8, 1), 60);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTraineeProfile(credentials, "unknown")).thenReturn(Optional.empty());

        //when //then
        assertThrows(EntityNotFoundException.class, () -> controller.createTraining(request, createRequest));
    }

    @Test
    @DisplayName("createTraining: missing trainer throws EntityNotFoundException")
    void createTraining_missingTrainer_shouldThrow() {
        //given
        CreateTrainingRequest createRequest = new CreateTrainingRequest(
                "John.Doe", "unknown", "Morning Strength Session", LocalDate.of(2026, 8, 1), 60);
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTraineeProfile(credentials, "John.Doe")).thenReturn(Optional.of(trainee));
        when(facade.getTrainerProfile(credentials, "unknown")).thenReturn(Optional.empty());

        //when //then
        assertThrows(EntityNotFoundException.class, () -> controller.createTraining(request, createRequest));
    }

    @Test
    @DisplayName("getTraineeTrainings: resolves credentials and returns mapped list")
    void getTraineeTrainings_shouldReturnMappedList() {
        //given
        Training training = new Training();
        TrainingResponse mappedResponse = new TrainingResponse(1L, "John.Doe", "Anna.Lee",
                "Morning Strength Session", "Strength", LocalDate.of(2026, 8, 1), 60);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTraineeTrainings(credentials, "John.Doe", null, null, null, null))
                .thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(mappedResponse);

        //when
        List<TrainingResponse> response = controller.getTraineeTrainings(
                request, "John.Doe", null, null, null, null);

        //then
        assertEquals(1, response.size());
        assertEquals(mappedResponse, response.get(0));
    }

    @Test
    @DisplayName("getTraineeTrainings: applies all optional filters and returns empty list")
    void getTraineeTrainings_withFilters_shouldReturnEmptyList() {
        //given
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTraineeTrainings(credentials, "John.Doe", fromDate, toDate, "Anna.Lee", "Cardio"))
                .thenReturn(List.of());

        //when
        List<TrainingResponse> response = controller.getTraineeTrainings(
                request, "John.Doe", fromDate, toDate, "Anna.Lee", "Cardio");

        //then
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("getTrainerTrainings: resolves credentials and returns mapped list")
    void getTrainerTrainings_shouldReturnMappedList() {
        //given
        Training training = new Training();
        TrainingResponse mappedResponse = new TrainingResponse(1L, "John.Doe", "Anna.Lee",
                "Morning Strength Session", "Strength", LocalDate.of(2026, 8, 1), 60);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTrainerTrainings(credentials, "Anna.Lee", null, null, null))
                .thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(mappedResponse);

        //when
        List<TrainingResponse> response = controller.getTrainerTrainings(
                request, "Anna.Lee", null, null, null);

        //then
        assertEquals(1, response.size());
        assertEquals(mappedResponse, response.get(0));
    }

    @Test
    @DisplayName("getTrainerTrainings: applies all optional filters and returns empty list")
    void getTrainerTrainings_withFilters_shouldReturnEmptyList() {
        //given
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTrainerTrainings(credentials, "Anna.Lee", fromDate, toDate, "John.Doe"))
                .thenReturn(List.of());

        //when
        List<TrainingResponse> response = controller.getTrainerTrainings(
                request, "Anna.Lee", fromDate, toDate, "John.Doe");

        //then
        assertEquals(0, response.size());
    }
}