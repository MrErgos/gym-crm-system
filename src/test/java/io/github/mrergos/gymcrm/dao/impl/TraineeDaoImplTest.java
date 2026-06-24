package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.entity.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraineeDaoImpl tests")
class TraineeDaoImplTest {

    private TraineeDaoImpl dao;
    private Map<Long, Trainee> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        dao = new TraineeDaoImpl();
        dao.setStorage(storage);
    }

    private Trainee buildTrainee(Long id, String firstName, String lastName) {
        Trainee t = new Trainee();
        t.setUserId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("password123");
        t.setActive(true);
        t.setDateOfBirth(LocalDate.of(1990, 1, 1));
        t.setAddress("Test Address");
        return t;
    }

    @Test
    @DisplayName("save: assigns id when null and stores trainee")
    void save_newTrainee_shouldAssignIdAndStore() {
        //given
        Trainee trainee = buildTrainee(null, "John", "Doe");

        //when
        Trainee saved = dao.save(trainee);

        //then
        assertNotNull(saved.getUserId());
        assertEquals(1L, saved.getUserId());
        assertTrue(storage.containsKey(saved.getUserId()));
    }

    @Test
    @DisplayName("save: keeps existing id on update")
    void save_existingTrainee_shouldUpdateInStorage() {
        //given
        Trainee trainee = buildTrainee(5L, "John", "Doe");
        dao.save(trainee);

        //when
        trainee.setAddress("New Address");
        dao.save(trainee);

        //then
        assertEquals(1, storage.size());
        assertEquals("New Address", storage.get(5L).getAddress());
    }

    @Test
    @DisplayName("save: generates sequential ids for multiple trainees")
    void save_multipleNewTrainees_shouldGenerateSequentialIds() {
        //given
        //when
        Trainee t1 = dao.save(buildTrainee(null, "John", "Doe"));
        Trainee t2 = dao.save(buildTrainee(null, "Alice", "Smith"));
        Trainee t3 = dao.save(buildTrainee(null, "Bob", "Jones"));

        //then
        assertEquals(1L, t1.getUserId());
        assertEquals(2L, t2.getUserId());
        assertEquals(3L, t3.getUserId());
    }

    @Test
    @DisplayName("save: returns defensive copy - mutating result does not affect storage")
    void save_shouldReturnDefensiveCopy() {
        //given
        Trainee saved = dao.save(buildTrainee(null, "John", "Doe"));

        //when
        saved.setAddress("Mutated Address");

        //then
        assertNotEquals("Mutated Address", storage.get(saved.getUserId()).getAddress());
    }

    @Test
    @DisplayName("save: storing defensively - mutating original does not affect storage")
    void save_shouldStoreDefensiveCopy() {
        //given
        Trainee original = buildTrainee(null, "John", "Doe");
        Trainee saved = dao.save(original);

        //when
        original.setAddress("Mutated Address");

        //then
        assertNotEquals("Mutated Address", storage.get(saved.getUserId()).getAddress());
    }

    @Test
    @DisplayName("findById: returns trainee when exists")
    void findById_existingId_shouldReturnTrainee() {
        //given
        Trainee saved = dao.save(buildTrainee(null, "John", "Doe"));

        //when
        Optional<Trainee> result = dao.findById(saved.getUserId());

        //then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    @DisplayName("findById: returns empty when not found")
    void findById_nonExistingId_shouldReturnEmpty() {
        //given
        //when
        Optional<Trainee> result = dao.findById(999L);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById: returns defensive copy - mutating result does not affect storage")
    void findById_shouldReturnDefensiveCopy() {
        //given
        Trainee saved = dao.save(buildTrainee(null, "John", "Doe"));

        //when
        Trainee found = dao.findById(saved.getUserId()).orElseThrow();
        found.setAddress("Mutated");

        //then
        assertNotEquals("Mutated", dao.findById(saved.getUserId()).orElseThrow().getAddress());
    }

    @Test
    @DisplayName("findAll: returns all stored trainees")
    void findAll_shouldReturnAllTrainees() {
        //given
        dao.save(buildTrainee(null, "John", "Doe"));
        dao.save(buildTrainee(null, "Alice", "Smith"));

        //when
        List<Trainee> all = dao.findAll();

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
    @DisplayName("delete: removes trainee from storage")
    void delete_existingId_shouldRemoveFromStorage() {
        //given
        Trainee saved = dao.save(buildTrainee(null, "John", "Doe"));

        //when
        dao.delete(saved.getUserId());

        //then
        assertFalse(storage.containsKey(saved.getUserId()));
    }

    @Test
    @DisplayName("delete: does not throw when id not found")
    void delete_nonExistingId_shouldNotThrow() {
        //given
        //when
        //then
        assertDoesNotThrow(() -> dao.delete(999L));
    }

    @Test
    @DisplayName("existsByUsername: returns true when username exists")
    void existsByUsername_existing_shouldReturnTrue() {
        //given
        //when
        dao.save(buildTrainee(null, "John", "Doe"));

        //then
        assertTrue(dao.existsByUsername("John.Doe"));
    }

    @Test
    @DisplayName("existsByUsername: returns false when username not found")
    void existsByUsername_nonExisting_shouldReturnFalse() {
        //given
        //when
        //then
        assertFalse(dao.existsByUsername("NonExistent.User"));
    }
}