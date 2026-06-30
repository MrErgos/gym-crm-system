package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainingDaoImpl tests")
class TrainingDaoImplTest {

    private TrainingDaoImpl dao;
    private Map<Long, Training> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        dao = new TrainingDaoImpl();
        dao.setStorage(storage);
    }

    private Training buildTraining(Long id, Long traineeId, Long trainerId) {
        Training t = new Training();
        t.setId(id);
        t.setTraineeId(traineeId);
        t.setTrainerId(trainerId);
        t.setTrainingName("Test Training");
        t.setTrainingType(new TrainingType("yoga"));
        t.setTrainingDate(LocalDate.of(2024, 6, 1));
        t.setTrainingDuration(60);
        return t;
    }

    @Test
    @DisplayName("save: assigns id when null and stores training")
    void save_newTraining_shouldAssignIdAndStore() {
        //given
        //when
        Training saved = dao.save(buildTraining(null, 1L, 1L));

        //then
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertTrue(storage.containsKey(saved.getId()));
    }

    @Test
    @DisplayName("save: generates sequential ids")
    void save_multipleTrainings_shouldGenerateSequentialIds() {
        //given
        //when
        Training t1 = dao.save(buildTraining(null, 1L, 1L));
        Training t2 = dao.save(buildTraining(null, 2L, 2L));

        //then
        assertEquals(1L, t1.getId());
        assertEquals(2L, t2.getId());
    }

    @Test
    @DisplayName("save: returns defensive copy - mutating trainingType does not affect storage")
    void save_shouldReturnDefensiveCopy() {
        //given
        Training saved = dao.save(buildTraining(null, 1L, 1L));

        //when
        saved.getTrainingType().setTrainingTypeName("mutated");

        //then
        assertNotEquals("mutated",
                storage.get(saved.getId()).getTrainingType().getTrainingTypeName());
    }

    @Test
    @DisplayName("save: storing defensively - mutating original does not affect storage")
    void save_shouldStoreDefensiveCopy() {
        //given
        Training original = buildTraining(null, 1L, 1L);
        Training saved = dao.save(original);

        //when
        original.getTrainingType().setTrainingTypeName("mutated");

        //then
        assertNotEquals("mutated",
                storage.get(saved.getId()).getTrainingType().getTrainingTypeName());
    }

    @Test
    @DisplayName("findById: returns training when exists")
    void findById_existingId_shouldReturnTraining() {
        //given
        Training saved = dao.save(buildTraining(null, 1L, 1L));

        //when
        Optional<Training> result = dao.findById(saved.getId());

        //then
        assertTrue(result.isPresent());
        assertEquals("Test Training", result.get().getTrainingName());
    }

    @Test
    @DisplayName("findById: returns empty when not found")
    void findById_nonExistingId_shouldReturnEmpty() {
        //given
        //when
        //then
        assertTrue(dao.findById(999L).isEmpty());
    }

    @Test
    @DisplayName("findById: returns defensive copy")
    void findById_shouldReturnDefensiveCopy() {
        //given
        Training saved = dao.save(buildTraining(null, 1L, 1L));

        //when
        Training found = dao.findById(saved.getId()).orElseThrow();
        found.getTrainingType().setTrainingTypeName("mutated");

        //then
        assertNotEquals("mutated",
                dao.findById(saved.getId()).orElseThrow().getTrainingType().getTrainingTypeName());
    }

    @Test
    @DisplayName("findAll: returns all stored trainings")
    void findAll_shouldReturnAll() {
        //given
        dao.save(buildTraining(null, 1L, 1L));
        dao.save(buildTraining(null, 2L, 2L));

        //when
        List<Training> all = dao.findAll();

        //then
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll: returns empty list when storage is empty")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        //given
        //when
        //then
        assertTrue(dao.findAll().isEmpty());
    }
}