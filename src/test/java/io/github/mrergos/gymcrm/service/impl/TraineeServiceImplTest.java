package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.service.UsernameGenerator;
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
@DisplayName("TraineeServiceImpl tests")
class TraineeServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @InjectMocks
    private TraineeServiceImpl service;


    private Trainee buildTrainee(Long id, String firstName, String lastName) {
        Trainee t = new Trainee();
        t.setId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("validPassword");
        t.setActive(true);
        return t;
    }

    @Test
    @DisplayName("createTraineeProfile: sets generated username, active=true, and saves")
    void createTraineeProfile_valid_shouldGenerateUsernameAndSave() {
        //given
        when(usernameGenerator.generate("John", "Doe")).thenReturn("John.Doe");
        when(traineeDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainee result = service.createTraineeProfile("John", "Doe",
                LocalDate.of(1990, 1, 1), "Address");

        //then
        assertEquals("John.Doe", result.getUsername());
        assertNotNull(result.getPassword());
        assertTrue(result.isActive());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    @DisplayName("createTraineeProfile: throws when firstName is blank")
    void createTraineeProfile_blankFirstName_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraineeProfile("", "Doe", null, null));
    }

    @Test
    @DisplayName("createTraineeProfile: throws when lastName is blank")
    void createTraineeProfile_blankLastName_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraineeProfile("John", "", null, null));
    }

    @Test
    @DisplayName("updateTraineeProfile: valid trainee - saves and returns result")
    void updateTraineeProfile_valid_shouldSaveAndReturn() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any())).thenReturn(trainee);

        //when
        Trainee result = service.updateTraineeProfile(trainee);

        //then
        assertEquals("John.Doe", result.getUsername());
        verify(traineeDao).save(trainee);
    }

    @Test
    @DisplayName("updateTraineeProfile: throws EntityNotFoundException when id not found")
    void updateTraineeProfile_nonExistingId_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(99L, "John", "Doe");
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.updateTraineeProfile(trainee));
    }

    @Test
    @DisplayName("updateTraineeProfile: throws when id is null")
    void updateTraineeProfile_nullId_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(null, "John", "Doe");

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(trainee));
    }

    @Test
    @DisplayName("updateTraineeProfile: throws when username is blank")
    void updateTraineeProfile_blankUsername_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        trainee.setUsername("");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(trainee));
    }

    @Test
    @DisplayName("updateTraineeProfile: new username does not exist - saves successfully")
    void updateTraineeProfile_newUsernameIsNotExists_shouldSave() {
        //given
        Trainee existing = buildTrainee(1L, "John", "Doe");
        existing.setUsername("John.Doe");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));

        Trainee updatedTrainee = buildTrainee(1L, "John", "Doe");
        updatedTrainee.setUsername("CoolJohn");

        when(usernameGenerator.checkUsernameExists("CoolJohn")).thenReturn(false);
        when(traineeDao.save(any())).thenReturn(updatedTrainee);

        //when
        Trainee result = service.updateTraineeProfile(updatedTrainee);

        //then
        assertEquals("CoolJohn", result.getUsername());
        verify(traineeDao).save(updatedTrainee);
    }

    @Test
    @DisplayName("updateTraineeProfile: throws when username is already exists")
    void updateTraineeProfile_usernameExists_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        trainee.setUsername("John.Doe");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee updatedTrainee = buildTrainee(1L, "John", "Doe");
        updatedTrainee.setUsername("CoolJohn");

        when(usernameGenerator.checkUsernameExists("CoolJohn")).thenReturn(true);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(updatedTrainee));
        verify(traineeDao, never()).save(any());
    }

    @Test
    @DisplayName("updateTraineeProfile: throws when password is blank")
    void updateTraineeProfile_blankPassword_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        trainee.setPassword("");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(trainee));
    }

    @Test
    @DisplayName("updateTraineeProfile: throws when password is too short")
    void updateTraineeProfile_passwordTooShort_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        trainee.setPassword("12345");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(trainee));
    }

    @Test
    @DisplayName("deleteTraineeProfile: existing username - deletes successfully")
    void deleteTraineeProfile_existingUsername_shouldDelete() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        //when
        service.deleteTraineeProfile("John.Doe");

        //then
        verify(traineeDao).delete(trainee);
    }

    @Test
    @DisplayName("deleteTraineeProfile: throws EntityNotFoundException when username not found")
    void deleteTraineeProfile_nonExistingUsername_shouldThrow() {
        //given
        when(traineeDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.deleteTraineeProfile("Unknown"));
        verify(traineeDao, never()).delete(any());
    }

    @Test
    @DisplayName("deleteTraineeProfile: throws when username is null")
    void deleteTraineeProfile_nullUsername_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteTraineeProfile(null));
    }

    @Test
    @DisplayName("getTraineeProfile: delegates to DAO and returns result")
    void getTraineeProfile_shouldDelegateToDao() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        //when
        Optional<Trainee> result = service.getTraineeProfile("John.Doe");

        //then
        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
    }

    @Test
    @DisplayName("getTraineeProfile: returns empty when not found")
    void getTraineeProfile_notFound_shouldReturnEmpty() {
        //given
        when(traineeDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertTrue(service.getTraineeProfile("Unknown").isEmpty());
    }

    @Test
    @DisplayName("getAllTrainees: delegates to DAO and returns list")
    void getAllTrainees_shouldReturnAll() {
        //given
        List<Trainee> expected = List.of(
                buildTrainee(1L, "John", "Doe"),
                buildTrainee(2L, "Alice", "Smith"));
        when(traineeDao.findAll()).thenReturn(expected);

        //when
        List<Trainee> result = service.getAllTrainees();

        //then
        assertEquals(2, result.size());
        verify(traineeDao).findAll();
    }

    @Test
    @DisplayName("changePassword: valid - updates password and saves")
    void changePassword_valid_shouldUpdateAndSave() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        //when
        service.changePassword("John.Doe", "newValidPassword");

        //then
        assertEquals("newValidPassword", trainee.getPassword());
        verify(traineeDao).save(trainee);
    }

    @Test
    @DisplayName("changePassword: throws when new password is blank")
    void changePassword_blankPassword_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("John.Doe", ""));
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    @DisplayName("changePassword: throws when new password is too short")
    void changePassword_passwordTooShort_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("John.Doe", "short"));
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    @DisplayName("changePassword: throws EntityNotFoundException when trainee not found")
    void changePassword_notFound_shouldThrow() {
        //given
        when(traineeDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.changePassword("Unknown", "newValidPassword"));
    }

    @Test
    @DisplayName("toggleActive: flips active status and saves")
    void toggleActive_valid_shouldFlipStatusAndSave() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        trainee.setActive(true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        //when
        service.toggleActive("John.Doe");

        //then
        assertFalse(trainee.isActive());
        verify(traineeDao).save(trainee);
    }

    @Test
    @DisplayName("toggleActive: throws EntityNotFoundException when trainee not found")
    void toggleActive_notFound_shouldThrow() {
        //given
        when(traineeDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.toggleActive("Unknown"));
    }

    @Test
    @DisplayName("getTrainersNotAssigned: delegates to TrainerDao and returns list")
    void getTrainersNotAssigned_shouldDelegateToTrainerDao() {
        //given
        Trainer trainer = new Trainer();
        trainer.setUsername("Jane.Smith");
        when(trainerDao.findAllNotAssignedToTrainee("John.Doe")).thenReturn(List.of(trainer));

        //when
        List<Trainer> result = service.getTrainersNotAssigned("John.Doe");

        //then
        assertEquals(1, result.size());
        assertEquals("Jane.Smith", result.get(0).getUsername());
        verify(trainerDao).findAllNotAssignedToTrainee("John.Doe");
    }

    @Test
    @DisplayName("updateTraineeTrainers: delegates to TraineeDao and returns updated list")
    void updateTraineeTrainers_valid_shouldDelegateToDao() {
        //given
        List<String> trainerUsernames = List.of("Jane.Smith", "Bob.Brown");
        Trainer trainer1 = new Trainer();
        trainer1.setUsername("Jane.Smith");
        Trainer trainer2 = new Trainer();
        trainer2.setUsername("Bob.Brown");
        List<Trainer> expected = List.of(trainer1, trainer2);

        when(traineeDao.updateTrainers("John.Doe", trainerUsernames)).thenReturn(expected);

        //when
        List<Trainer> result = service.updateTraineeTrainers("John.Doe", trainerUsernames);

        //then
        assertEquals(2, result.size());
        verify(traineeDao).updateTrainers("John.Doe", trainerUsernames);
    }

    @Test
    @DisplayName("updateTraineeTrainers: throws when trainer usernames list is null")
    void updateTraineeTrainers_nullList_shouldThrow() {
        //given
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeTrainers("John.Doe", null));
        verify(traineeDao, never()).updateTrainers(any(), any());
    }
}