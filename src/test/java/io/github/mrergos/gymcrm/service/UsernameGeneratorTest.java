package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsernameGenerator tests")
class UsernameGeneratorTest {
    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private UsernameGenerator generator;

    @Test
    @DisplayName("generate: returns base username when no conflicts")
    void generate_noConflict_shouldReturnBase() {
        //given
        when(traineeDao.existsByUsername(anyString())).thenReturn(false);
        when(trainerDao.existsByUsername(anyString())).thenReturn(false);

        //when
        String result = generator.generate("John", "Smith");

        //then
        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("generate: adds suffix '1' when base taken by trainee")
    void generate_traineeConflict_shouldAddSuffix() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);
        when(traineeDao.existsByUsername("John.Smith1")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith1")).thenReturn(false);

        //when
        String result = generator.generate("John", "Smith");

        //then
        assertEquals("John.Smith1", result);
    }

    @Test
    @DisplayName("generate: adds suffix '1' when base taken by trainer")
    void generate_trainerConflict_shouldAddSuffix() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith")).thenReturn(true);
        when(traineeDao.existsByUsername("John.Smith1")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith1")).thenReturn(false);

        //when
        String result = generator.generate("John", "Smith");

        //then
        assertEquals("John.Smith1", result);
    }

    @Test
    @DisplayName("generate: increments suffix when both storages have conflicts")
    void generate_multipleConflicts_shouldIncrementSuffix() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);
        when(traineeDao.existsByUsername("John.Smith1")).thenReturn(true);
        when(traineeDao.existsByUsername("John.Smith2")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith2")).thenReturn(false);

        //when
        String result = generator.generate("John", "Smith");

        //then
        assertEquals("John.Smith2", result);
    }

    @Test
    @DisplayName("generate: does not call trainerDao when traineeDao returns true")
    void generate_shortCircuit_shouldNotCallTrainerDaoIfTraineeDaoReturnsTrue() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);
        when(traineeDao.existsByUsername("John.Smith1")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith1")).thenReturn(false);

        //when
        generator.generate("John", "Smith");

        //then
        verify(trainerDao, never()).existsByUsername("John.Smith");
    }

    @Test
    @DisplayName("checkUsernameExists: returns false when neither dao has the username")
    void checkUsernameExists_noneHaveIt_shouldReturnFalse() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith")).thenReturn(false);

        //when
        boolean result = generator.checkUsernameExists("John.Smith");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("checkUsernameExists: returns true when traineeDao has the username (short-circuit)")
    void checkUsernameExists_traineeHasIt_shouldReturnTrue() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);

        //when
        boolean result = generator.checkUsernameExists("John.Smith");

        //then
        assertTrue(result);
        verify(trainerDao, never()).existsByUsername(anyString());
    }

    @Test
    @DisplayName("checkUsernameExists: returns true when trainerDao has the username")
    void checkUsernameExists_trainerHasIt_shouldReturnTrue() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Smith")).thenReturn(true);

        //when
        boolean result = generator.checkUsernameExists("John.Smith");

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("checkUsernameExists: returns true when both dao have the username")
    void checkUsernameExists_bothHaveIt_shouldReturnTrue() {
        //given
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);

        //when
        boolean result = generator.checkUsernameExists("John.Smith");

        //then
        assertTrue(result);
    }
}