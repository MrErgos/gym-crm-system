package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl tests")
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @InjectMocks
    private TrainerServiceImpl service;

    private Trainer buildTrainer(Long id, String firstName, String lastName) {
        Trainer t = new Trainer();
        t.setUserId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("validPassword");
        t.setActive(true);
        t.setSpecialization(new TrainingType("fitness"));
        return t;
    }

    @Test
    @DisplayName("createTrainerProfile: sets generated username, active=true, and saves")
    void createTrainerProfile_valid_shouldGenerateUsernameAndSave() {
        //given
        when(usernameGenerator.generate("Jane", "Smith")).thenReturn("Jane.Smith");
        when(trainerDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainer result = service.createTrainerProfile("Jane", "Smith", new TrainingType("yoga"));

        //then
        assertEquals("Jane.Smith", result.getUsername());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        assertTrue(result.isActive());
        assertEquals("yoga", result.getSpecialization().getTrainingTypeName());
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    @DisplayName("createTrainerProfile: throws when firstName is blank")
    void createTrainerProfile_blankFirstName_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("", "Smith", new TrainingType("yoga")));
    }

    @Test
    @DisplayName("createTrainerProfile: throws when lastName is blank")
    void createTrainerProfile_blankLastName_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("Jane", "", new TrainingType("yoga")));
    }

    @Test
    @DisplayName("createTrainerProfile: throws when specialization is null")
    void createTrainerProfile_nullSpecialization_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainerProfile("Jane", "Smith", null));
    }

    @Test
    @DisplayName("updateTrainerProfile: valid trainer - saves and returns result")
    void updateTrainerProfile_valid_shouldSaveAndReturn() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(any())).thenReturn(trainer);

        //when
        Trainer result = service.updateTrainerProfile(trainer);

        //then
        assertEquals("Jane.Smith", result.getUsername());
        verify(trainerDao).save(trainer);
    }

    @Test
    @DisplayName("updateTrainerProfile: throws EntityNotFoundException when id not found")
    void updateTrainerProfile_nonExistingId_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(99L, "Jane", "Smith");
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when id is null")
    void updateTrainerProfile_nullId_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(null, "Jane", "Smith");

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when username is blank")
    void updateTrainerProfile_blankUsername_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setUsername("");
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when username is already exists")
    void updateTrainerProfile_usernameExists_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setUsername("CoolJane");
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(usernameGenerator.checkUsernameExists("CoolJane")).thenReturn(true);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when username is blank")
    void updateTrainerProfile_blankPassword_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setPassword("");
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when password is too short")
    void updateTrainerProfile_passwordTooShort_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setPassword("12345");
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainerProfile(trainer));
    }

    @Test
    @DisplayName("updateTrainerProfile: throws when specialization is null")
    void updateTrainerProfile_nullSpecialization_shouldThrow() {
        //given
        Trainer trainer = buildTrainer(1L, "Jane", "Smith");
        trainer.setSpecialization(null);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

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
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        //when
        Optional<Trainer> result = service.getTrainerProfile(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("Jane.Smith", result.get().getUsername());
    }

    @Test
    @DisplayName("getTrainerProfile: returns empty when not found")
    void getTrainerProfile_notFound_shouldReturnEmpty() {
        //given
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertTrue(service.getTrainerProfile(99L).isEmpty());
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
}