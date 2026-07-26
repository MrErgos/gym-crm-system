package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.dao.TrainingDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.metrics.GymMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl tests")
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private GymMetrics gymMetrics;

    @InjectMocks
    private TrainingServiceImpl service;

    private Training buildTraining(Trainee trainee, Trainer trainer) {
        Training t = new Training();
        t.setTrainee(trainee);
        t.setTrainer(trainer);
        t.setTrainingName("Morning Yoga");
        t.setTrainingType(new TrainingType("yoga"));
        t.setTrainingDate(LocalDate.of(2024, 6, 1));
        t.setTrainingDuration(60);
        return t;
    }
    private Trainee buildTrainee(Long id) {
        Trainee t = new Trainee();
        t.setId(id);
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setUsername("John.Doe");
        t.setPassword("pass");
        t.setTrainers(new HashSet<>());
        return t;
    }

    private Trainer buildTrainer(Long id) {
        Trainer t = new Trainer();
        t.setId(id);
        t.setFirstName("Jane");
        t.setLastName("Smith");
        t.setUsername("Jane.Smith");
        t.setPassword("pass");
        t.setSpecialization(new TrainingType("yoga"));
        return t;
    }

    @Test
    @DisplayName("createTraining: valid input - saves and returns training")
    void createTraining_valid_shouldSaveAndReturn() {
        //given
        Trainee trainee = buildTrainee(1L);
        Trainer trainer = buildTrainer(1L);
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainingDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        Training result = service.createTraining(training);

        //then
        assertNotNull(result);
        assertEquals("Morning Yoga", result.getTrainingName());
        assertTrue(trainee.getTrainers().contains(trainer));
        verify(trainingDao).save(training);
        verify(gymMetrics).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainee not found")
    void createTraining_nonExistingTrainee_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(99L), buildTrainer(1L));
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.createTraining(training));
        verify(trainingDao, never()).save(any());
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainer not found")
    void createTraining_nonExistingTrainer_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(99L));
        when(traineeDao.findById(1L)).thenReturn(Optional.of(buildTrainee(1L)));
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.createTraining(training));
        verify(trainingDao, never()).save(any());
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when training is null")
    void createTraining_null_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(null));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when training id is not null")
    void createTraining_nonNullId_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));
        training.setId(5L);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(trainingDao, never()).save(any());
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainee is null")
    void createTraining_nullTrainee_shouldThrow() {
        //given
        Training training = buildTraining(null, buildTrainer(1L));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainer is null")
    void createTraining_nullTrainer_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), null);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainingName is blank")
    void createTraining_blankTrainingName_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));
        training.setTrainingName("");

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainingType is null")
    void createTraining_nullTrainingType_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));
        training.setTrainingType(null);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainingDate is null")
    void createTraining_nullTrainingDate_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));
        training.setTrainingDate(null);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: throws when trainingDuration is null")
    void createTraining_nullTrainingDuration_shouldThrow() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));
        training.setTrainingDuration(null);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("createTraining: trainer already assigned - does not duplicate in trainers list")
    void createTraining_trainerAlreadyAssigned_shouldNotDuplicate() {
        //given
        Trainer trainer = buildTrainer(1L);
        Trainee trainee = buildTrainee(1L);
        trainee.getTrainers().add(trainer);

        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainingDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        service.createTraining(training);

        //then
        assertEquals(1, trainee.getTrainers().size());
        verify(gymMetrics).incrementTrainingsCreated();
    }

    @Test
    @DisplayName("getTraining: delegates to DAO and returns result")
    void getTraining_shouldDelegateToDao() {
        //given
        Training training = buildTraining(buildTrainee(1L), buildTrainer(1L));
        training.setId(1L);
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        //when
        Optional<Training> result = service.getTraining(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("Morning Yoga", result.get().getTrainingName());
    }

    @Test
    @DisplayName("getTraining: returns empty when not found")
    void getTraining_notFound_shouldReturnEmpty() {
        //given
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertTrue(service.getTraining(99L).isEmpty());
    }

    @Test
    @DisplayName("getAllTrainings: delegates to DAO and returns list")
    void getAllTrainings_shouldReturnAll() {
        //given
        List<Training> expected = List.of(
                buildTraining(buildTrainee(1L), buildTrainer(1L)),
                buildTraining(buildTrainee(2L), buildTrainer(2L)));
        when(trainingDao.findAll()).thenReturn(expected);

        //when
        List<Training> result = service.getAllTrainings();

        //then
        assertEquals(2, result.size());
        verify(trainingDao).findAll();
    }

    @Test
    @DisplayName("getTraineeTrainings: delegates to DAO with all filters and returns list")
    void getTraineeTrainings_valid_shouldDelegateToDao() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> expected = List.of(buildTraining(buildTrainee(1L), buildTrainer(1L)));

        when(trainingDao.findTraineeTrainings("John.Doe", from, to, "Jane.Smith", "yoga"))
                .thenReturn(expected);

        //when
        List<Training> result = service.getTraineeTrainings("John.Doe", from, to, "Jane.Smith", "yoga");

        //then
        assertEquals(1, result.size());
        verify(trainingDao).findTraineeTrainings("John.Doe", from, to, "Jane.Smith", "yoga");
    }

    @Test
    @DisplayName("getTraineeTrainings: works with null optional filters")
    void getTraineeTrainings_nullFilters_shouldDelegateToDao() {
        //given
        List<Training> expected = List.of(buildTraining(buildTrainee(1L), buildTrainer(1L)));
        when(trainingDao.findTraineeTrainings("John.Doe", null, null, null, null))
                .thenReturn(expected);

        //when
        List<Training> result = service.getTraineeTrainings("John.Doe", null, null, null, null);

        //then
        assertEquals(1, result.size());
        verify(trainingDao).findTraineeTrainings("John.Doe", null, null, null, null);
    }

    @Test
    @DisplayName("getTraineeTrainings: throws when traineeUsername is blank")
    void getTraineeTrainings_blankUsername_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.getTraineeTrainings("", null, null, null, null));
        verify(trainingDao, never()).findTraineeTrainings(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("getTrainerTrainings: delegates to DAO with all filters and returns list")
    void getTrainerTrainings_valid_shouldDelegateToDao() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> expected = List.of(buildTraining(buildTrainee(1L), buildTrainer(1L)));

        when(trainingDao.findTrainerTrainings("Jane.Smith", from, to, "John.Doe"))
                .thenReturn(expected);

        //when
        List<Training> result = service.getTrainerTrainings("Jane.Smith", from, to, "John.Doe");

        //then
        assertEquals(1, result.size());
        verify(trainingDao).findTrainerTrainings("Jane.Smith", from, to, "John.Doe");
    }

    @Test
    @DisplayName("getTrainerTrainings: works with null optional filters")
    void getTrainerTrainings_nullFilters_shouldDelegateToDao() {
        //given
        List<Training> expected = List.of(buildTraining(buildTrainee(1L), buildTrainer(1L)));
        when(trainingDao.findTrainerTrainings("Jane.Smith", null, null, null))
                .thenReturn(expected);

        //when
        List<Training> result = service.getTrainerTrainings("Jane.Smith", null, null, null);

        //then
        assertEquals(1, result.size());
        verify(trainingDao).findTrainerTrainings("Jane.Smith", null, null, null);
    }

    @Test
    @DisplayName("getTrainerTrainings: throws when trainerUsername is blank")
    void getTrainerTrainings_blankUsername_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerTrainings("", null, null, null));
        verify(trainingDao, never()).findTrainerTrainings(any(), any(), any(), any());
    }
}