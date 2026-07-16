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
        Trainee input = new Trainee();
        input.setFirstName("John");
        input.setLastName("Doe");
        input.setDateOfBirth(LocalDate.of(1990, 1, 1));
        input.setAddress("Address");

        when(usernameGenerator.generate("John", "Doe")).thenReturn("John.Doe");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainee result = service.createTraineeProfile(input);

        //then
        assertEquals("John.Doe", result.getUsername());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        assertTrue(result.isActive());
        verify(traineeDao).save(input);
    }

    @Test
    @DisplayName("createTraineeProfile: throws when firstName is blank")
    void createTraineeProfile_blankFirstName_shouldThrow() {
        //given
        Trainee input = new Trainee();
        input.setFirstName("");
        input.setLastName("Doe");

        //when
        // then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraineeProfile(input));
    }

    @Test
    @DisplayName("createTraineeProfile: throws when lastName is blank")
    void createTraineeProfile_blankLastName_shouldThrow() {
        //given
        Trainee input = new Trainee();
        input.setFirstName("John");
        input.setLastName("");

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.createTraineeProfile(input));
    }

    @Test
    @DisplayName("updateTraineeProfile: valid trainee - saves and returns result")
    void updateTraineeProfile_valid_shouldSaveAndReturn() {
        //given
        Trainee existing = buildTrainee(1L, "John", "Doe");
        existing.setDateOfBirth(LocalDate.of(1990, 1, 1));
        existing.setAddress("Old Address");

        Trainee updateRequest = new Trainee();
        updateRequest.setUsername("John.Doe");
        updateRequest.setFirstName("Johnny");
        updateRequest.setLastName("Doey");
        updateRequest.setDateOfBirth(LocalDate.of(1991, 2, 2));
        updateRequest.setAddress("New Address");
        updateRequest.setActive(false);

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainee result = service.updateTraineeProfile(updateRequest);

        //then
        assertEquals("Johnny", result.getFirstName());
        assertEquals("Doey", result.getLastName());
        assertEquals(LocalDate.of(1991, 2, 2), result.getDateOfBirth());
        assertEquals("New Address", result.getAddress());
        assertFalse(result.isActive());
        verify(traineeDao).save(existing);
    }

    @Test
    @DisplayName("updateTraineeProfile: throws EntityNotFoundException when username not found")
    void updateTraineeProfile_nonExistingUsername_shouldThrow() {
        //given
        Trainee updateRequest = new Trainee();
        updateRequest.setUsername("Nonexistent.User");

        when(traineeDao.findByUsername("Nonexistent.User")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.updateTraineeProfile(updateRequest));
    }

    @Test
    @DisplayName("updateTraineeProfile: throws when username is null")
    void updateTraineeProfile_nullUsername_shouldThrow() {
        //given
        Trainee updateRequest = new Trainee();
        updateRequest.setUsername(null);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(updateRequest));
    }

    @Test
    @DisplayName("updateTraineeProfile: retains existing address and birth date when update fields are null")
    void updateTraineeProfile_nullOptionalFields_shouldRetainExistingValues() {
        //given
        Trainee existing = buildTrainee(1L, "John", "Doe");
        existing.setDateOfBirth(LocalDate.of(1990, 1, 1));
        existing.setAddress("Preserved Address");

        Trainee updateRequest = new Trainee();
        updateRequest.setUsername("John.Doe");
        updateRequest.setFirstName("Johnny");
        updateRequest.setLastName("Doe");
        updateRequest.setDateOfBirth(null);
        updateRequest.setAddress(null);

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Trainee result = service.updateTraineeProfile(updateRequest);

        //then
        assertEquals("Preserved Address", result.getAddress());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        verify(traineeDao).save(existing);
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

    @Test
    @DisplayName("existsByUsername: delegates to TraineeDao and returns true if exists")
    void existsByUsername_shouldReturnTrue_whenExists() {
        //given
        when(traineeDao.existsByUsername("John.Doe")).thenReturn(true);

        //when
        boolean result = service.existsByUsername("John.Doe");

        //then
        assertTrue(result);
        verify(traineeDao).existsByUsername("John.Doe");
    }

    @Test
    @DisplayName("existsByUsername: delegates to TraineeDao and returns false if not exists")
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        //given
        when(traineeDao.existsByUsername("Unknown")).thenReturn(false);

        //when
        boolean result = service.existsByUsername("Unknown");

        //then
        assertFalse(result);
        verify(traineeDao).existsByUsername("Unknown");
    }
}