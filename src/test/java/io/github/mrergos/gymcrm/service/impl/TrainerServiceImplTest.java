package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.dao.TrainingTypeDao;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.metrics.GymMetrics;
import io.github.mrergos.gymcrm.service.UsernameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl tests")
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private GymMetrics gymMetrics;

    @InjectMocks
    private TrainerServiceImpl service;

    private Trainer buildTrainer(Long id, String firstName, String lastName) {
        Trainer t = new Trainer();
        t.setId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("validPassword");
        t.setActive(true);
        t.setSpecialization(new TrainingType(1L,"fitness"));
        return t;
    }

    @Test
    @DisplayName("createTrainerProfile: sets generated username, active=true, and saves")
    void createTrainerProfile_valid_shouldGenerateUsernameAndSave() {
        //given
        when(usernameGenerator.generate("Jane", "Smith")).thenReturn("Jane.Smith");
        when(trainerDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainer result = service.createTrainerProfile("Jane", "Smith", new TrainingType(1L,"yoga"));

        //then
        assertEquals("Jane.Smith", result.getUsername());
        assertNotNull(result.getPassword());
        assertTrue(result.isActive());
        assertEquals("yoga", result.getSpecialization().getTrainingTypeName());
        verify(trainerDao).save(any(Trainer.class));
        verify(gymMetrics).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("createTrainerProfile: throws when firstName is blank")
    void createTrainerProfile_blankFirstName_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("", "Smith", new TrainingType(1L, "yoga")));
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("createTrainerProfile: throws when lastName is blank")
    void createTrainerProfile_blankLastName_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("Jane", "", new TrainingType(1L,"yoga")));
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("createTrainerProfile: throws when specialization is null")
    void createTrainerProfile_nullSpecialization_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("Jane", "Smith", (TrainingType) null));
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("createTrainerProfile with ID: fetches specialization from DB and saves successfully")
    void createTrainerProfileWithId_valid_shouldFetchTypeAndSave() {
        //given
        TrainingType specialization = new TrainingType(1L,"yoga");
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(specialization));
        when(usernameGenerator.generate("Jane", "Smith")).thenReturn("Jane.Smith");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainer result = service.createTrainerProfile("Jane", "Smith", 1L);

        //then
        assertEquals("Jane.Smith", result.getUsername());
        assertEquals("yoga", result.getSpecialization().getTrainingTypeName());
        verify(trainingTypeDao).findById(1L);
        verify(trainerDao).save(any(Trainer.class));
        verify(gymMetrics).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("createTrainerProfile with ID: throws EntityNotFoundException when specialization ID not found")
    void createTrainerProfileWithId_nonExistingId_shouldThrow() {
        //given
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        //when / then
        assertThrows(EntityNotFoundException.class,
                () -> service.createTrainerProfile("Jane", "Smith", 99L));
        verify(trainerDao, never()).save(any());
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("createTrainerProfile with ID: throws IllegalArgumentException when parameters are blank/null")
    void createTrainerProfileWithId_invalidParams_shouldThrow() {
        // Assertions are checked before the DB call
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("", "Smith", 1L));
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("Jane", "", 1L));
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("Jane", "Smith", (Long) null));
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    @DisplayName("updateTrainerProfile: valid trainer - saves and returns result")
    void updateTrainerProfile_valid_shouldSaveAndReturn() {
        //given
        Trainer existing = buildTrainer(1L, "Jane", "Smith");
        existing.setSpecialization(new TrainingType(1L,"oldSpec"));

        Trainer updateRequest = new Trainer();
        updateRequest.setUsername("Jane.Smith");
        updateRequest.setFirstName("Janet");
        updateRequest.setLastName("Smithy");
        updateRequest.setSpecialization(new TrainingType(1L, "newSpec"));
        updateRequest.setActive(false);

        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.of(existing));
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainer result = service.updateTrainerProfile(updateRequest);

        //then
        assertEquals("Janet", result.getFirstName());
        assertEquals("Smithy", result.getLastName());
        assertEquals("newSpec", result.getSpecialization().getTrainingTypeName());
        assertFalse(result.isActive());
        verify(trainerDao).save(existing);
    }

    @Test
    @DisplayName("updateTrainerProfile: throws EntityNotFoundException when username not found")
    void updateTrainerProfile_nonExistingUsername_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setUsername("Nonexistent.Trainer");
        when(trainerDao.findByUsername("Nonexistent.Trainer")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when username is blank")
    void updateTrainerProfile_blankUsername_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setUsername("");

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("getTrainerProfile: delegates to DAO and returns result")
    void getTrainerProfile_shouldDelegateToDao() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        //when
        Optional<Trainer> result = service.getTrainerProfile("Jane.Smith");

        //then
        assertTrue(result.isPresent());
        assertEquals("Jane.Smith", result.get().getUsername());
    }

    @Test
    @DisplayName("getTrainerProfile: returns empty when not found")
    void getTrainerProfile_notFound_shouldReturnEmpty() {
        //given
        when(trainerDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertTrue(service.getTrainerProfile("Unknown").isEmpty());
    }

    @Test
    @DisplayName("getAllTrainers: delegates to DAO and returns list")
    void getAllTrainers_shouldReturnAll() {
        //given
        List<Trainer> expected = List.of(
                buildTrainer(1L, "Jane", "Smith"),
                buildTrainer(2L, "Bob", "Jones"));
        when(trainerDao.findAll()).thenReturn(expected);

        //when
        List<Trainer> result = service.getAllTrainers();

        //then
        assertEquals(2, result.size());
        verify(trainerDao).findAll();
    }


    @Test
    @DisplayName("changePassword: valid - updates password and saves")
    void changePassword_valid_shouldUpdateAndSave() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        //when
        service.changePassword("Jane.Smith", "newValidPassword");

        //then
        assertEquals("newValidPassword", trainer.getPassword());
        verify(trainerDao).save(trainer);
    }

    @Test
    @DisplayName("changePassword: throws when new password is blank")
    void changePassword_blankPassword_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("Jane.Smith", ""));
        verify(trainerDao, never()).findByUsername(any());
    }

    @Test
    @DisplayName("changePassword: throws when new password is too short")
    void changePassword_passwordTooShort_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("Jane.Smith", "short"));
        verify(trainerDao, never()).findByUsername(any());
    }

    @Test
    @DisplayName("changePassword: throws EntityNotFoundException when trainer not found")
    void changePassword_notFound_shouldThrow() {
        //given
        when(trainerDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.changePassword("Unknown", "newValidPassword"));
    }

    @Test
    @DisplayName("toggleActive: flips active status and saves")
    void toggleActive_valid_shouldFlipStatusAndSave() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setActive(true);
        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        //when
        service.toggleActive("Jane.Smith");

        //then
        assertFalse(trainer.isActive());
        verify(trainerDao).save(trainer);
    }

    @Test
    @DisplayName("toggleActive: throws EntityNotFoundException when trainer not found")
    void toggleActive_notFound_shouldThrow() {
        //given
        when(trainerDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.toggleActive("Unknown"));
    }

    @Test
    @DisplayName("getAvailableTrainingTypes: delegates to TrainingTypeDao and returns list")
    void getAvailableTrainingTypes_shouldReturnAll() {
        //given
        List<TrainingType> expected = List.of(
                new TrainingType(1L,"fitness"),
                new TrainingType(2L,"yoga"));
        when(trainingTypeDao.findAll()).thenReturn(expected);

        //when
        List<TrainingType> result = service.getAvailableTrainingTypes();

        //then
        assertEquals(2, result.size());
        verify(trainingTypeDao).findAll();
    }

    @Test
    @DisplayName("getTrainingTypeById: delegates to TrainingTypeDao and returns type")
    void getTrainingTypeById_shouldDelegateToDaoAndReturn() {
        //given
        TrainingType type = new TrainingType(1L,"yoga");
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(type));

        //when
        Optional<TrainingType> result = service.getTrainingTypeById(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("yoga", result.get().getTrainingTypeName());
        verify(trainingTypeDao).findById(1L);
    }

    @Test
    @DisplayName("existsByUsername: delegates to TrainerDao and returns true if exists")
    void existsByUsername_shouldReturnTrue_whenExists() {
        //given
        when(trainerDao.existsByUsername("Jane.Smith")).thenReturn(true);

        //when
        boolean result = service.existsByUsername("Jane.Smith");

        //then
        assertTrue(result);
        verify(trainerDao).existsByUsername("Jane.Smith");
    }

    @Test
    @DisplayName("existsByUsername: delegates to TrainerDao and returns false if not exists")
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        //given
        when(trainerDao.existsByUsername("Unknown")).thenReturn(false);

        //when
        boolean result = service.existsByUsername("Unknown");

        //then
        assertFalse(result);
        verify(trainerDao).existsByUsername("Unknown");
    }
}