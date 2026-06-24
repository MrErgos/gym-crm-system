package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.dao.TrainingDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
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
@DisplayName("TrainingServiceImpl tests")
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private TrainingServiceImpl service;

    private Training buildTraining(Long traineeId, Long trainerId) {
        Training t = new Training();
        t.setTraineeId(traineeId);
        t.setTrainerId(trainerId);
        t.setTrainingName("Morning Yoga");
        t.setTrainingType(new TrainingType("yoga"));
        t.setTrainingDate(LocalDate.of(2024, 6, 1));
        t.setTrainingDuration(60);
        return t;
    }

    private Trainee buildTrainee(Long id) {
        Trainee t = new Trainee();
        t.setUserId(id);
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setUsername("John.Doe");
        t.setPassword("pass");
        return t;
    }

    private Trainer buildTrainer(Long id) {
        Trainer t = new Trainer();
        t.setUserId(id);
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
        Training training = buildTraining(1L, 1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(buildTrainee(1L)));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(buildTrainer(1L)));
        when(trainingDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        Training result = service.createTraining(training);

        //then
        assertNotNull(result);
        assertEquals("Morning Yoga", result.getTrainingName());
        verify(trainingDao).save(training);
    }

    @Test
    @DisplayName("createTraining: throws when trainee not found")
    void createTraining_nonExistingTrainee_shouldThrow() {
        //given
        Training training = buildTraining(99L, 1L);
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.createTraining(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    @DisplayName("createTraining: throws when trainer not found")
    void createTraining_nonExistingTrainer_shouldThrow() {
        //given
        Training training = buildTraining(1L, 99L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(buildTrainee(1L)));
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.createTraining(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    @DisplayName("createTraining: throws when training is null")
    void createTraining_null_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(null));
    }

    @Test
    @DisplayName("createTraining: throws when training id is not null")
    void createTraining_nonNullId_shouldThrow() {
        Training training = buildTraining(1L, 1L);
        training.setId(5L);

        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
        verify(trainingDao, never()).save(any());
    }


    @Test
    @DisplayName("createTraining: throws when traineeId is null")
    void createTraining_nullTraineeId_shouldThrow() {
        //given
        Training training = buildTraining(null, 1L);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
    }

    @Test
    @DisplayName("createTraining: throws when trainerId is null")
    void createTraining_nullTrainerId_shouldThrow() {
        //given
        Training training = buildTraining(1L, null);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
    }

    @Test
    @DisplayName("createTraining: throws when trainingName is blank")
    void createTraining_blankTrainingName_shouldThrow() {
        //given
        Training training = buildTraining(1L, 1L);
        training.setTrainingName("");

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraining(training));
    }

    @Test
    @DisplayName("getTraining: delegates to DAO and returns result")
    void getTraining_shouldDelegateToDao() {
        //given
        Training training = buildTraining(1L, 1L);
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
                buildTraining(1L, 1L),
                buildTraining(2L, 2L));
        when(trainingDao.findAll()).thenReturn(expected);

        //when
        List<Training> result = service.getAllTrainings();

        //then
        assertEquals(2, result.size());
        verify(trainingDao).findAll();
    }
}