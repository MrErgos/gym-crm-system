package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.exception.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl tests")
class AuthenticationServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private AuthenticationServiceImpl service;

    private Trainee buildTrainee(String username, String password) {
        Trainee t = new Trainee();
        t.setUsername(username);
        t.setPassword(password);
        return t;
    }

    private Trainer buildTrainer(String username, String password) {
        Trainer t = new Trainer();
        t.setUsername(username);
        t.setPassword(password);
        return t;
    }

    @Test
    @DisplayName("authenticate: trainee found with matching password - does not throw")
    void authenticate_traineeCorrectPassword_shouldNotThrow() {
        //given
        when(traineeDao.findByUsername("John.Doe"))
                .thenReturn(Optional.of(buildTrainee("John.Doe", "validPassword")));

        //when
        //then
        assertDoesNotThrow(() -> service.authenticate("John.Doe", "validPassword"));
        verify(trainerDao, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("authenticate: trainee found with wrong password - throws AuthenticationException")
    void authenticate_traineeWrongPassword_shouldThrow() {
        //given
        when(traineeDao.findByUsername("John.Doe"))
                .thenReturn(Optional.of(buildTrainee("John.Doe", "validPassword")));

        //when
        //then
        assertThrows(AuthenticationException.class,
                () -> service.authenticate("John.Doe", "wrongPassword"));
        verify(trainerDao, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("authenticate: not a trainee, trainer found with matching password - does not throw")
    void authenticate_trainerCorrectPassword_shouldNotThrow() {
        //given
        when(traineeDao.findByUsername("Jane.Smith")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Jane.Smith"))
                .thenReturn(Optional.of(buildTrainer("Jane.Smith", "validPassword")));

        //when
        //then
        assertDoesNotThrow(() -> service.authenticate("Jane.Smith", "validPassword"));
    }

    @Test
    @DisplayName("authenticate: not a trainee, trainer found with wrong password - throws AuthenticationException")
    void authenticate_trainerWrongPassword_shouldThrow() {
        //given
        when(traineeDao.findByUsername("Jane.Smith")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Jane.Smith"))
                .thenReturn(Optional.of(buildTrainer("Jane.Smith", "validPassword")));

        //when
        //then
        assertThrows(AuthenticationException.class,
                () -> service.authenticate("Jane.Smith", "wrongPassword"));
    }

    @Test
    @DisplayName("authenticate: username not found in either trainees or trainers - throws AuthenticationException")
    void authenticate_userNotFound_shouldThrow() {
        //given
        when(traineeDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(AuthenticationException.class,
                () -> service.authenticate("Nobody.Here", "anyPassword"));
    }
}