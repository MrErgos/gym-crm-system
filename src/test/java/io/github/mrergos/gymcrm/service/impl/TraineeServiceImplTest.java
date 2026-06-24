package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.entity.Trainee;
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
    private UsernameGenerator usernameGenerator;

    @InjectMocks
    private TraineeServiceImpl service;

    private Trainee buildTrainee(Long id, String firstName, String lastName) {
        Trainee t = new Trainee();
        t.setUserId(id);
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
        assertEquals(10, result.getPassword().length());
        assertTrue(result.isActive());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    @DisplayName("createTraineeProfile: throws when firstName is blank")
    void createTraineeProfile_blankFirstName_shouldThrow() {
        //given
        //then
        //when
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
        when(usernameGenerator.checkUsernameExists("John.Doe")).thenReturn(false);
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
    @DisplayName("updateTraineeProfile: throws when username is already exists")
    void updateTraineeProfile_usernameExists_shouldThrow() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        trainee.setUsername("CoolJohn");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(usernameGenerator.checkUsernameExists("CoolJohn")).thenReturn(true);

        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTraineeProfile(trainee));
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
    @DisplayName("deleteTraineeProfile: existing id - deletes successfully")
    void deleteTraineeProfile_existingId_shouldDelete() {
        //given
        when(traineeDao.findById(1L)).thenReturn(Optional.of(buildTrainee(1L, "John", "Doe")));

        //when
        service.deleteTraineeProfile(1L);

        //then
        verify(traineeDao).delete(1L);
    }

    @Test
    @DisplayName("deleteTraineeProfile: throws EntityNotFoundException when id not found")
    void deleteTraineeProfile_nonExistingId_shouldThrow() {
        //given
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class,
                () -> service.deleteTraineeProfile(99L));
        verify(traineeDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("deleteTraineeProfile: throws when id is null")
    void deleteTraineeProfile_nullId_shouldThrow() {
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
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        //when
        Optional<Trainee> result = service.getTraineeProfile(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
    }

    @Test
    @DisplayName("getTraineeProfile: returns empty when not found")
    void getTraineeProfile_notFound_shouldReturnEmpty() {
        //given
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        //when
        //then
        assertTrue(service.getTraineeProfile(99L).isEmpty());
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
}