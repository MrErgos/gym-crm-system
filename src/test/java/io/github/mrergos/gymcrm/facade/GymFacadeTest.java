package io.github.mrergos.gymcrm.facade;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.service.TraineeService;
import io.github.mrergos.gymcrm.service.TrainerService;
import io.github.mrergos.gymcrm.service.TrainingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymFacade tests")
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymFacade facade;

    @Test
    @DisplayName("createTraineeProfile: delegates to TraineeService")
    void createTraineeProfile_shouldDelegateToService() {
        //given
        Trainee expected = new Trainee();
        expected.setUsername("John.Doe");
        when(traineeService.createTraineeProfile(any(), any(), any(), any())).thenReturn(expected);

        //when
        Trainee result = facade.createTraineeProfile("John", "Doe",
                LocalDate.of(1990, 1, 1), "Address");

        //then
        assertEquals("John.Doe", result.getUsername());
        verify(traineeService).createTraineeProfile("John", "Doe",
                LocalDate.of(1990, 1, 1), "Address");
    }

    @Test
    @DisplayName("updateTraineeProfile: delegates to TraineeService")
    void updateTraineeProfile_shouldDelegateToService() {
        //given
        Trainee trainee = new Trainee();
        when(traineeService.updateTraineeProfile(trainee)).thenReturn(trainee);

        //when
        facade.updateTraineeProfile(trainee);

        //then
        verify(traineeService).updateTraineeProfile(trainee);
    }

    @Test
    @DisplayName("deleteTraineeProfile: delegates to TraineeService")
    void deleteTraineeProfile_shouldDelegateToService() {
        //given
        //when
        facade.deleteTraineeProfile(1L);

        //then
        verify(traineeService).deleteTraineeProfile(1L);
    }

    @Test
    @DisplayName("getTraineeProfile: delegates to TraineeService")
    void getTraineeProfile_shouldDelegateToService() {
        //given
        when(traineeService.getTraineeProfile(1L)).thenReturn(Optional.empty());

        //when
        Optional<Trainee> result = facade.getTraineeProfile(1L);

        //then
        assertTrue(result.isEmpty());
        verify(traineeService).getTraineeProfile(1L);
    }

    @Test
    @DisplayName("getAllTrainees: delegates to TraineeService")
    void getAllTrainees_shouldDelegateToService() {
        //given
        when(traineeService.getAllTrainees()).thenReturn(List.of());

        //when
        facade.getAllTrainees();

        //then
        verify(traineeService).getAllTrainees();
    }

    @Test
    @DisplayName("createTrainerProfile: delegates to TrainerService")
    void createTrainerProfile_shouldDelegateToService() {
        //given
        Trainer expected = new Trainer();
        expected.setUsername("Jane.Smith");
        TrainingType type = new TrainingType("yoga");
        when(trainerService.createTrainerProfile(any(), any(), any())).thenReturn(expected);

        //when
        Trainer result = facade.createTrainerProfile("Jane", "Smith", type);

        //then
        assertEquals("Jane.Smith", result.getUsername());
        verify(trainerService).createTrainerProfile("Jane", "Smith", type);
    }

    @Test
    @DisplayName("updateTrainerProfile: delegates to TrainerService")
    void updateTrainerProfile_shouldDelegateToService() {
        //given
        Trainer trainer = new Trainer();
        when(trainerService.updateTrainerProfile(trainer)).thenReturn(trainer);

        //when
        facade.updateTrainerProfile(trainer);

        //then
        verify(trainerService).updateTrainerProfile(trainer);
    }

    @Test
    @DisplayName("getTrainerProfile: delegates to TrainerService")
    void getTrainerProfile_shouldDelegateToService() {
        //given
        when(trainerService.getTrainerProfile(1L)).thenReturn(Optional.empty());

        //when
        facade.getTrainerProfile(1L);

        //then
        verify(trainerService).getTrainerProfile(1L);
    }

    @Test
    @DisplayName("getAllTrainers: delegates to TrainerService")
    void getAllTrainers_shouldDelegateToService() {
        //given
        when(trainerService.getAllTrainers()).thenReturn(List.of());

        //when
        facade.getAllTrainers();

        //then
        verify(trainerService).getAllTrainers();
    }

    @Test
    @DisplayName("createTraining: delegates to TrainingService")
    void createTraining_shouldDelegateToService() {
        //given
        Training training = new Training();
        when(trainingService.createTraining(training)).thenReturn(training);

        //when
        facade.createTraining(training);

        //then
        verify(trainingService).createTraining(training);
    }

    @Test
    @DisplayName("getTraining: delegates to TrainingService")
    void getTraining_shouldDelegateToService() {
        //given
        when(trainingService.getTraining(1L)).thenReturn(Optional.empty());

        //when
        facade.getTraining(1L);

        //then
        verify(trainingService).getTraining(1L);
    }

    @Test
    @DisplayName("getAllTrainings: delegates to TrainingService")
    void getAllTrainings_shouldDelegateToService() {
        //given
        when(trainingService.getAllTrainings()).thenReturn(List.of());

        //when
        facade.getAllTrainings();

        //then
        verify(trainingService).getAllTrainings();
    }
}