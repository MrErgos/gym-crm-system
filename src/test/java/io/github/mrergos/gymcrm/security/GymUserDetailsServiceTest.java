package io.github.mrergos.gymcrm.security;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymUserDetailsService tests")
class GymUserDetailsServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private GymUserDetailsService service;

    private Trainee buildTrainee(String username, boolean active) {
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setPassword("encodedPassword");
        trainee.setActive(active);
        return trainee;
    }

    private Trainer buildTrainer(String username, boolean active) {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setPassword("encodedPassword");
        trainer.setActive(active);
        return trainer;
    }

    @Test
    @DisplayName("loadUserByUsername: returns trainee details with ROLE_TRAINEE when trainee exists")
    void loadUserByUsername_traineeExists_shouldReturnTraineeDetails() {
        //given
        Trainee trainee = buildTrainee("John.Doe", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        //when
        UserDetails result = service.loadUserByUsername("John.Doe");

        //then
        assertEquals("John.Doe", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINEE")));
        assertTrue(result.isEnabled());
        verifyNoInteractions(trainerDao);
    }

    @Test
    @DisplayName("loadUserByUsername: returns trainer details with ROLE_TRAINER when trainee not found but trainer exists")
    void loadUserByUsername_trainerExists_shouldReturnTrainerDetails() {
        //given
        Trainer trainer = buildTrainer("Anna.Lee", true);
        when(traineeDao.findByUsername("Anna.Lee")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Anna.Lee")).thenReturn(Optional.of(trainer));

        //when
        UserDetails result = service.loadUserByUsername("Anna.Lee");

        //then
        assertEquals("Anna.Lee", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER")));
    }

    @Test
    @DisplayName("loadUserByUsername: inactive trainee is returned as disabled")
    void loadUserByUsername_inactiveTrainee_shouldReturnDisabled() {
        //given
        Trainee trainee = buildTrainee("John.Doe", false);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        //when
        UserDetails result = service.loadUserByUsername("John.Doe");

        //then
        assertFalse(result.isEnabled());
    }

    @Test
    @DisplayName("loadUserByUsername: throws UsernameNotFoundException when neither trainee nor trainer exists")
    void loadUserByUsername_neitherExists_shouldThrow() {
        //given
        when(traineeDao.findByUsername("Unknown")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("Unknown"));
    }
}