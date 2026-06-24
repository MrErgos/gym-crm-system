package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainerDaoImpl tests")
class TrainerDaoImplTest {

    private TrainerDaoImpl dao;
    private Map<Long, Trainer> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        dao = new TrainerDaoImpl();
        dao.setStorage(storage);
    }

    private Trainer buildTrainer(Long id, String firstName, String lastName) {
        Trainer t = new Trainer();
        t.setUserId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("password123");
        t.setActive(true);
        t.setSpecialization(new TrainingType("fitness"));
        return t;
    }

    @Test
    @DisplayName("save: assigns id when null and stores trainer")
    void save_newTrainer_shouldAssignIdAndStore() {
        //given
        //when
        Trainer saved = dao.save(buildTrainer(null, "Jane", "Smith"));

        //then
        assertNotNull(saved.getUserId());
        assertEquals(1L, saved.getUserId());
        assertTrue(storage.containsKey(saved.getUserId()));
    }

    @Test
    @DisplayName("save: keeps existing id on update")
    void save_existingTrainer_shouldUpdateInStorage() {
        //given
        dao.save(buildTrainer(10L, "Jane", "Smith"));

        //when
        Trainer updated = buildTrainer(10L, "Jane", "Smith");
        updated.setSpecialization(new TrainingType("yoga"));
        dao.save(updated);

        //then
        assertEquals(1, storage.size());
        assertEquals("yoga", storage.get(10L).getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("save: returns defensive copy - mutating result does not affect storage")
    void save_shouldReturnDefensiveCopy() {
        //given
        Trainer saved = dao.save(buildTrainer(null, "Jane", "Smith"));

        //when
        saved.getSpecialization().setTrainingTypeName("mutated");

        //then
        assertNotEquals("mutated", storage.get(saved.getUserId()).getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("save: storing defensively - mutating original does not affect storage")
    void save_shouldStoreDefensiveCopy() {
        //given
        Trainer original = buildTrainer(null, "Jane", "Smith");
        Trainer saved = dao.save(original);

        //when
        original.getSpecialization().setTrainingTypeName("mutated");

        //then
        assertNotEquals("mutated", storage.get(saved.getUserId()).getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findById: returns trainer when exists")
    void findById_existingId_shouldReturnTrainer() {
        //given
        Trainer saved = dao.save(buildTrainer(null, "Jane", "Smith"));

        //when
        Optional<Trainer> result = dao.findById(saved.getUserId());

        //then
        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());
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
    @DisplayName("findById: returns defensive copy - mutating result does not affect storage")
    void findById_shouldReturnDefensiveCopy() {
        //given
        Trainer saved = dao.save(buildTrainer(null, "Jane", "Smith"));

        //when
        Trainer found = dao.findById(saved.getUserId()).orElseThrow();
        found.getSpecialization().setTrainingTypeName("mutated");

        //then
        assertNotEquals("mutated",
                dao.findById(saved.getUserId()).orElseThrow().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findAll: returns all stored trainers")
    void findAll_shouldReturnAll() {
        //given
        dao.save(buildTrainer(null, "Jane", "Smith"));
        dao.save(buildTrainer(null, "Bob", "Jones"));

        //when
        List<Trainer> all = dao.findAll();

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

    @Test
    @DisplayName("existsByUsername: returns true when username exists")
    void existsByUsername_existing_shouldReturnTrue() {
        //given
        //when
        dao.save(buildTrainer(null, "Jane", "Smith"));

        //then
        assertTrue(dao.existsByUsername("Jane.Smith"));
    }

    @Test
    @DisplayName("existsByUsername: returns false when username not found")
    void existsByUsername_nonExisting_shouldReturnFalse() {
        //given
        //when
        //then
        assertFalse(dao.existsByUsername("NonExistent.Trainer"));
    }
}