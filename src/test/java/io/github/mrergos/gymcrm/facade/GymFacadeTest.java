package io.github.mrergos.gymcrm.facade;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.service.AuthenticationService;
import io.github.mrergos.gymcrm.service.TraineeService;
import io.github.mrergos.gymcrm.service.TrainerService;
import io.github.mrergos.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private GymFacade facade;

    private static final Credentials CREDENTIALS = new Credentials("John.Doe", "validPassword");

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(authenticationService).authenticate(anyString(), anyString());
    }

    @Test
    @DisplayName("login: authenticates credentials successfully")
    void login_shouldAuthenticateSuccessfully() {
        // when
        facade.login(CREDENTIALS);

        // then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
    }

    @Test
    @DisplayName("createTraineeProfile: delegates to TraineeService without authentication")
    void createTraineeProfile_shouldDelegateToServiceWithoutAuth() {
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
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("getAvailableTrainingTypes: delegates to TrainerService without authentication")
    void getAvailableTrainingTypes_shouldDelegateToServiceWithoutAuth() {
        //given
        List<TrainingType> expected = List.of(new TrainingType("yoga"), new TrainingType("fitness"));
        when(trainerService.getAvailableTrainingTypes()).thenReturn(expected);

        //when
        List<TrainingType> result = facade.getAvailableTrainingTypes();

        //then
        assertEquals(2, result.size());
        verify(trainerService).getAvailableTrainingTypes();
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("updateTraineeProfile: authenticates then delegates to TraineeService")
    void updateTraineeProfile_shouldAuthenticateAndDelegateToService() {
        //given
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        when(traineeService.updateTraineeProfile(trainee)).thenReturn(trainee);

        //when
        Trainee result = facade.updateTraineeProfile(CREDENTIALS, trainee);

        //then
        assertEquals("John.Doe", result.getUsername());
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).updateTraineeProfile(trainee);
    }

    @Test
    @DisplayName("deleteTraineeProfile: authenticates then delegates to TraineeService")
    void deleteTraineeProfile_shouldAuthenticateAndDelegateToService() {
        //given
        //when
        facade.deleteTraineeProfile(CREDENTIALS, "Alice.Smith");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).deleteTraineeProfile("Alice.Smith");
    }

    @Test
    @DisplayName("getTraineeProfile: authenticates then delegates to TraineeService")
    void getTraineeProfile_shouldAuthenticateAndDelegateToService() {
        //given
        Trainee trainee = new Trainee();
        when(traineeService.getTraineeProfile("Alice.Smith")).thenReturn(Optional.of(trainee));

        //when
        Optional<Trainee> result = facade.getTraineeProfile(CREDENTIALS, "Alice.Smith");

        //then
        assertTrue(result.isPresent());
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).getTraineeProfile("Alice.Smith");
    }

    @Test
    @DisplayName("getAllTrainees: authenticates then delegates to TraineeService")
    void getAllTrainees_shouldAuthenticateAndDelegateToService() {
        //given
        when(traineeService.getAllTrainees()).thenReturn(List.of());

        //when
        facade.getAllTrainees(CREDENTIALS);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).getAllTrainees();
    }

    @Test
    @DisplayName("getTrainersNotAssigned: authenticates then delegates to TraineeService")
    void getTrainersNotAssigned_shouldAuthenticateAndDelegateToService() {
        //given
        when(traineeService.getTrainersNotAssigned("Alice.Smith")).thenReturn(List.of());

        //when
        facade.getTrainersNotAssigned(CREDENTIALS, "Alice.Smith");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).getTrainersNotAssigned("Alice.Smith");
    }

    @Test
    @DisplayName("updateTraineeProfile: propagates authentication failure and does not call service")
    void updateTraineeProfile_authFailure_shouldNotCallService() {
        //given
        Trainee trainee = new Trainee();
        doThrow(new RuntimeException("Invalid credentials"))
                .when(authenticationService).authenticate("John.Doe", "validPassword");

        //when
        //then
        assertThrows(RuntimeException.class, () -> facade.updateTraineeProfile(CREDENTIALS, trainee));
        verifyNoInteractions(traineeService);
    }

    @Test
    @DisplayName("updateTraineeTrainers: authenticates then delegates to TraineeService")
    void updateTraineeTrainers_shouldAuthenticateAndDelegateToService() {
        //given
        List<String> trainerUsernames = List.of("Jane.Smith", "Bob.Jones");
        when(traineeService.updateTraineeTrainers("Alice.Smith", trainerUsernames)).thenReturn(List.of());

        //when
        facade.updateTraineeTrainers(CREDENTIALS, "Alice.Smith", trainerUsernames);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).updateTraineeTrainers("Alice.Smith", trainerUsernames);
    }

    @Test
    @DisplayName("changeTraineePassword: authenticates then delegates to TraineeService")
    void changeTraineePassword_shouldAuthenticateAndDelegateToService() {
        //given
        //when
        facade.changeTraineePassword(CREDENTIALS, "newValidPassword");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).changePassword("John.Doe", "newValidPassword");
    }

    @Test
    @DisplayName("toggleTraineeActive: authenticates then delegates to TraineeService")
    void toggleTraineeActive_shouldAuthenticateAndDelegateToService() {
        //given
        //when
        facade.toggleTraineeActive(CREDENTIALS, "Alice.Smith");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).toggleActive("Alice.Smith");
    }


    @Test
    @DisplayName("createTrainerProfile with specialization ID: delegates to TrainerService without authentication")
    void createTrainerProfileWithId_shouldDelegateToServiceWithoutAuth() {
        //given
        Trainer expected = new Trainer();
        expected.setUsername("Jane.Smith");
        when(trainerService.createTrainerProfile("Jane", "Smith", 1L)).thenReturn(expected);

        //when
        Trainer result = facade.createTrainerProfile("Jane", "Smith", 1L);

        //then
        assertEquals("Jane.Smith", result.getUsername());
        verify(trainerService).createTrainerProfile("Jane", "Smith", 1L);
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("createTrainerProfile with TrainingType: delegates to TrainerService without authentication")
    void createTrainerProfileWithSpecialization_shouldDelegateToServiceWithoutAuth() {
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
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("updateTrainerProfile: authenticates then delegates to TrainerService")
    void updateTrainerProfile_shouldAuthenticateAndDelegateToService() {
        //given
        Trainer trainer = new Trainer();
        when(trainerService.updateTrainerProfile(trainer)).thenReturn(trainer);

        //when
        facade.updateTrainerProfile(CREDENTIALS, trainer);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainerService).updateTrainerProfile(trainer);
    }

    @Test
    @DisplayName("getTrainerProfile: authenticates then delegates to TrainerService")
    void getTrainerProfile_shouldAuthenticateAndDelegateToService() {
        //given
        when(trainerService.getTrainerProfile("Jane.Smith")).thenReturn(Optional.empty());

        //when
        facade.getTrainerProfile(CREDENTIALS, "Jane.Smith");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainerService).getTrainerProfile("Jane.Smith");
    }

    @Test
    @DisplayName("getAllTrainers: authenticates then delegates to TrainerService")
    void getAllTrainers_shouldAuthenticateAndDelegateToService() {
        //given
        when(trainerService.getAllTrainers()).thenReturn(List.of());

        //when
        facade.getAllTrainers(CREDENTIALS);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainerService).getAllTrainers();
    }

    @Test
    @DisplayName("changeTrainerPassword: authenticates then delegates to TrainerService")
    void changeTrainerPassword_shouldAuthenticateAndDelegateToService() {
        //given
        //when
        facade.changeTrainerPassword(CREDENTIALS, "newValidPassword");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainerService).changePassword("John.Doe", "newValidPassword");
    }

    @Test
    @DisplayName("toggleTrainerActive: authenticates then delegates to TrainerService")
    void toggleTrainerActive_shouldAuthenticateAndDelegateToService() {
        //given
        //when
        facade.toggleTrainerActive(CREDENTIALS, "Jane.Smith");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainerService).toggleActive("Jane.Smith");
    }


    @Test
    @DisplayName("createTraining: authenticates then delegates to TrainingService")
    void createTraining_shouldAuthenticateAndDelegateToService() {
        //given
        Training training = new Training();
        training.setTrainingName("Morning Yoga");
        when(trainingService.createTraining(training)).thenReturn(training);

        //when
        facade.createTraining(CREDENTIALS, training);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainingService).createTraining(training);
    }

    @Test
    @DisplayName("getTraining: authenticates then delegates to TrainingService")
    void getTraining_shouldAuthenticateAndDelegateToService() {
        //given
        when(trainingService.getTraining(1L)).thenReturn(Optional.empty());

        //when
        facade.getTraining(CREDENTIALS, 1L);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainingService).getTraining(1L);
    }

    @Test
    @DisplayName("getAllTrainings: authenticates then delegates to TrainingService")
    void getAllTrainings_shouldAuthenticateAndDelegateToService() {
        //given
        when(trainingService.getAllTrainings()).thenReturn(List.of());

        //when
        facade.getAllTrainings(CREDENTIALS);

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainingService).getAllTrainings();
    }

    @Test
    @DisplayName("getTraineeTrainings: authenticates then delegates to TrainingService with all filters")
    void getTraineeTrainings_shouldAuthenticateAndDelegateToService() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        when(trainingService.getTraineeTrainings("Alice.Smith", from, to, "Jane.Smith", "yoga"))
                .thenReturn(List.of());

        //when
        facade.getTraineeTrainings(CREDENTIALS, "Alice.Smith", from, to, "Jane.Smith", "yoga");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainingService).getTraineeTrainings("Alice.Smith", from, to, "Jane.Smith", "yoga");
    }

    @Test
    @DisplayName("getTrainerTrainings: authenticates then delegates to TrainingService with all filters")
    void getTrainerTrainings_shouldAuthenticateAndDelegateToService() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        when(trainingService.getTrainerTrainings("Jane.Smith", from, to, "Alice.Smith"))
                .thenReturn(List.of());

        //when
        facade.getTrainerTrainings(CREDENTIALS, "Jane.Smith", from, to, "Alice.Smith");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(trainingService).getTrainerTrainings("Jane.Smith", from, to, "Alice.Smith");
    }

    @Test
    @DisplayName("getTrainingTypeById: delegates to TrainerService without authentication")
    void getTrainingTypeById_shouldDelegateToServiceWithoutAuth() {
        //given
        TrainingType type = new TrainingType("yoga");
        when(trainerService.getTrainingTypeById(1L)).thenReturn(Optional.of(type));

        //when
        Optional<TrainingType> result = facade.getTrainingTypeById(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("yoga", result.get().getTrainingTypeName());
        verify(trainerService).getTrainingTypeById(1L);
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("changePassword: targets traineeService when trainee exists")
    void changePassword_shouldDelegateToTraineeService_whenTraineeExists() {
        //given
        when(traineeService.existsByUsername("John.Doe")).thenReturn(true);

        //when
        facade.changePassword(CREDENTIALS, "newSecurePassword");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
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
        facade.changePassword(CREDENTIALS, "newSecurePassword");

        //then
        verify(authenticationService).authenticate("John.Doe", "validPassword");
        verify(traineeService).existsByUsername("John.Doe");
        verify(trainerService).changePassword("John.Doe", "newSecurePassword");
        verify(traineeService, never()).changePassword(anyString(), anyString());
    }
}