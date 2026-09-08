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
        Trainee traineeInput = new Trainee();
        traineeInput.setFirstName("John");
        traineeInput.setLastName("Doe");

        Trainee expected = new Trainee();
        expected.setUsername("John.Doe");
        when(traineeService.createTraineeProfile(traineeInput)).thenReturn(expected);

        //when
        Trainee result = facade.createTraineeProfile(traineeInput);

        //then
        assertEquals("John.Doe", result.getUsername());
        verify(traineeService).createTraineeProfile(traineeInput);
    }

    @Test
    @DisplayName("getAvailableTrainingTypes: delegates to TrainerService")
    void getAvailableTrainingTypes_shouldDelegateToService() {
        //given
        List<TrainingType> expected = List.of(new TrainingType("yoga"), new TrainingType("fitness"));
        when(trainerService.getAvailableTrainingTypes()).thenReturn(expected);

        //when
        List<TrainingType> result = facade.getAvailableTrainingTypes();

        //then
        assertEquals(2, result.size());
        verify(trainerService).getAvailableTrainingTypes();
    }

    @Test
    @DisplayName("updateTraineeProfile: delegates to TraineeService")
    void updateTraineeProfile_shouldDelegateToService() {
        //given
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        when(traineeService.updateTraineeProfile(trainee)).thenReturn(trainee);

        //when
        Trainee result = facade.updateTraineeProfile(trainee);

        //then
        assertEquals("John.Doe", result.getUsername());
        verify(traineeService).updateTraineeProfile(trainee);
    }

    @Test
    @DisplayName("deleteTraineeProfile: delegates to TraineeService")
    void deleteTraineeProfile_shouldDelegateToService() {
        //given
        //when
        facade.deleteTraineeProfile("Alice.Smith");

        //then
        verify(traineeService).deleteTraineeProfile("Alice.Smith");
    }

    @Test
    @DisplayName("getTraineeProfile: delegates to TraineeService")
    void getTraineeProfile_shouldDelegateToService() {
        //given
        Trainee trainee = new Trainee();
        when(traineeService.getTraineeProfile("Alice.Smith")).thenReturn(Optional.of(trainee));

        //when
        Optional<Trainee> result = facade.getTraineeProfile("Alice.Smith");

        //then
        assertTrue(result.isPresent());
        verify(traineeService).getTraineeProfile("Alice.Smith");
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
    @DisplayName("getTrainersNotAssigned: delegates to TraineeService")
    void getTrainersNotAssigned_shouldDelegateToService() {
        //given
        when(traineeService.getTrainersNotAssigned("Alice.Smith")).thenReturn(List.of());

        //when
        facade.getTrainersNotAssigned("Alice.Smith");

        //then
        verify(traineeService).getTrainersNotAssigned("Alice.Smith");
    }

    @Test
    @DisplayName("updateTraineeTrainers: delegates to TraineeService")
    void updateTraineeTrainers_shouldDelegateToService() {
        //given
        List<String> trainerUsernames = List.of("Jane.Smith", "Bob.Jones");
        when(traineeService.updateTraineeTrainers("Alice.Smith", trainerUsernames)).thenReturn(List.of());

        //when
        facade.updateTraineeTrainers("Alice.Smith", trainerUsernames);

        //then
        verify(traineeService).updateTraineeTrainers("Alice.Smith", trainerUsernames);
    }

    @Test
    @DisplayName("toggleTraineeActive: delegates to TraineeService")
    void toggleTraineeActive_shouldDelegateToService() {
        //given
        //when
        facade.toggleTraineeActive("Alice.Smith");

        //then
        verify(traineeService).toggleActive("Alice.Smith");
    }

    @Test
    @DisplayName("createTrainerProfile with specialization ID: delegates to TrainerService")
    void createTrainerProfileWithId_shouldDelegateToService() {
        //given
        Trainer expected = new Trainer();
        expected.setUsername("Jane.Smith");
        when(trainerService.createTrainerProfile("Jane", "Smith", 1L)).thenReturn(expected);

        //when
        Trainer result = facade.createTrainerProfile("Jane", "Smith", 1L);

        //then
        assertEquals("Jane.Smith", result.getUsername());
        verify(trainerService).createTrainerProfile("Jane", "Smith", 1L);
    }

    @Test
    @DisplayName("createTrainerProfile with TrainingType: delegates to TrainerService")
    void createTrainerProfileWithSpecialization_shouldDelegateToService() {
        //given
        Trainer expected = new Trainer();
        expected.setUsername("Jane.Smith");
        TrainingType type = new TrainingType("yoga");
        when(trainerService.createTrainerProfile("Jane", "Smith", type)).thenReturn(expected);

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
        when(trainerService.getTrainerProfile("Jane.Smith")).thenReturn(Optional.empty());

        //when
        facade.getTrainerProfile("Jane.Smith");

        //then
        verify(trainerService).getTrainerProfile("Jane.Smith");
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
    @DisplayName("toggleTrainerActive: delegates to TrainerService")
    void toggleTrainerActive_shouldDelegateToService() {
        //given
        //when
        facade.toggleTrainerActive("Jane.Smith");

        //then
        verify(trainerService).toggleActive("Jane.Smith");
    }

    @Test
    @DisplayName("createTraining: delegates to TrainingService")
    void createTraining_shouldDelegateToService() {
        //given
        Training training = new Training();
        training.setTrainingName("Morning Yoga");
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

    @Test
    @DisplayName("getTraineeTrainings: delegates to TrainingService with all filters")
    void getTraineeTrainings_shouldDelegateToService() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        when(trainingService.getTraineeTrainings("Alice.Smith", from, to, "Jane.Smith", "yoga"))
                .thenReturn(List.of());

        //when
        facade.getTraineeTrainings("Alice.Smith", from, to, "Jane.Smith", "yoga");

        //then
        verify(trainingService).getTraineeTrainings("Alice.Smith", from, to, "Jane.Smith", "yoga");
    }

    @Test
    @DisplayName("getTrainerTrainings: delegates to TrainingService with all filters")
    void getTrainerTrainings_shouldDelegateToService() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        when(trainingService.getTrainerTrainings("Jane.Smith", from, to, "Alice.Smith"))
                .thenReturn(List.of());

        //when
        facade.getTrainerTrainings("Jane.Smith", from, to, "Alice.Smith");

        //then
        verify(trainingService).getTrainerTrainings("Jane.Smith", from, to, "Alice.Smith");
    }

    @Test
    @DisplayName("getTrainingTypeById: delegates to TrainerService")
    void getTrainingTypeById_shouldDelegateToService() {
        //given
        TrainingType type = new TrainingType("yoga");
        when(trainerService.getTrainingTypeById(1L)).thenReturn(Optional.of(type));

        //when
        Optional<TrainingType> result = facade.getTrainingTypeById(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("yoga", result.get().getTrainingTypeName());
        verify(trainerService).getTrainingTypeById(1L);
    }

    @Test
    @DisplayName("changePassword: targets traineeService when trainee exists")
    void changePassword_shouldDelegateToTraineeService_whenTraineeExists() {
        //given
        when(traineeService.existsByUsername("John.Doe")).thenReturn(true);

        //when
        facade.changePassword("John.Doe", "newSecurePassword");

        //then
        verify(traineeService).existsByUsername("John.Doe");
        verify(traineeService).changePassword("John.Doe", "newSecurePassword");
        verifyNoInteractions(trainerService);
    }

    @Test
    @DisplayName("changePassword: targets trainerService when trainee does not exist")
    void changePassword_shouldDelegateToTrainerService_whenTraineeDoesNotExist() {
        //given
        when(traineeService.existsByUsername("John.Doe")).thenReturn(false);

        //when
        facade.changePassword("John.Doe", "newSecurePassword");

        //then
        verify(traineeService).existsByUsername("John.Doe");
        verify(trainerService).changePassword("John.Doe", "newSecurePassword");
        verify(traineeService, never()).changePassword(anyString(), anyString());
    }
}