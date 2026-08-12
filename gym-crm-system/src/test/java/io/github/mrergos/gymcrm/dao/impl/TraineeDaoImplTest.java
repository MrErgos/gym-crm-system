package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeDaoImpl tests")
class TraineeDaoImplTest {

    private static final String FIND_BY_ID_HQL =
            "FROM Trainee t LEFT JOIN FETCH t.trainers tr LEFT JOIN FETCH tr.specialization WHERE t.id = :id";
    private static final String FIND_BY_USERNAME_HQL =
            "FROM Trainee t LEFT JOIN FETCH t.trainers tr LEFT JOIN FETCH tr.specialization WHERE t.username = :username";
    private static final String TRAINEE_TRAINERS_FETCH_HQL =
            "FROM Trainee t LEFT JOIN FETCH t.trainers WHERE t.username = :username";
    private static final String TRAINERS_BY_USERNAMES_HQL =
            "FROM Trainer t JOIN FETCH t.specialization WHERE t.username IN :usernames";

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Query<Trainee> query;
    
    @InjectMocks
    private TraineeDaoImpl dao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }
    
    private Trainee buildTrainee(Long id, String firstName, String lastName) {
        Trainee t = new Trainee();
        t.setId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("password123");
        t.setActive(true);
        t.setDateOfBirth(LocalDate.of(1990, 1, 1));
        t.setAddress("Test Address");
        return t;
    }

    private Trainer buildTrainer(Long id, String firstName, String lastName) {
        Trainer t = new Trainer();
        t.setId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("password123");
        t.setActive(true);
        t.setSpecialization(new TrainingType(1L,"Fitness"));
        return t;
    }

    @Test
    @DisplayName("save: return same trainee when persist trainee")
    void save_newTrainee_shouldCallPersist() {
        //given
        Trainee trainee = buildTrainee(null, "John", "Doe");

        //when
        Trainee saved = dao.save(trainee);

        //then
        assertSame(saved, trainee);
        verify(session).persist(trainee);
        verifyNoMoreInteractions(session);
    }

    @Test
    @DisplayName("save: return trainee when call merge")
    void save_existingTrainee_shouldCallMerge() {
        //given
        Trainee trainee = buildTrainee(5L, "John", "Doe");
        when(session.merge(trainee)).thenReturn(trainee);

        //when
        dao.save(trainee);

        //then
        verify(session).merge(trainee);
        verifyNoMoreInteractions(session);
    }

    @Test
    @DisplayName("findById: returns trainee when exists")
    void findById_existingId_shouldReturnTrainee() {
        //given
        Long traineeId = 1L;
        Trainee saved = buildTrainee(traineeId, "John", "Doe");

        when(session.createQuery(FIND_BY_ID_HQL, Trainee.class)).thenReturn(query);
        when(query.setParameter("id", traineeId)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(saved));

        //when
        Optional<Trainee> result = dao.findById(traineeId);

        //then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());

        verify(session).createQuery(FIND_BY_ID_HQL, Trainee.class);
        verify(query).setParameter("id", traineeId);
    }

    @Test
    @DisplayName("findById: returns empty when not found")
    void findById_nonExistingId_shouldReturnEmpty() {
        //given
        Long traineeId = 999L;

        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter("id", traineeId)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        Optional<Trainee> result = dao.findById(traineeId);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUsername: returns trainee when exists")
    void findByUsername_existingUsername_shouldReturnTrainee() {
        //given
        String username = "John.Doe";
        Trainee saved = buildTrainee(1L, "John", "Doe");

        when(session.createQuery(FIND_BY_USERNAME_HQL, Trainee.class)).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(saved));

        //when
        Optional<Trainee> result = dao.findByUsername(username);

        //then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());

        verify(session).createQuery(FIND_BY_USERNAME_HQL, Trainee.class);
        verify(query).setParameter("username", username);
        verify(query).uniqueResultOptional();
        verifyNoMoreInteractions(query);

    }

    @Test
    @DisplayName("findByUsername: returns empty when not found")
    void findByUsername_nonExistingUsername_shouldReturnEmpty() {
        //given
        String username = "John.Doe";

        when(session.createQuery(FIND_BY_USERNAME_HQL, Trainee.class)).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        Optional<Trainee> result = dao.findByUsername(username);

        //then
        assertTrue(result.isEmpty());

        verify(session).createQuery(FIND_BY_USERNAME_HQL, Trainee.class);
        verify(query).setParameter("username", username);
        verify(query).uniqueResultOptional();
        verifyNoMoreInteractions(query);

    }

    @Test
    @DisplayName("findAll: returns all stored trainees")
    void findAll_shouldReturnAllTrainees() {
        //given
        List<Trainee> traineeList = List.of(buildTrainee(null, "John", "Doe"),
                buildTrainee(null, "Alice", "Smith"));

        when(session.createQuery("FROM Trainee", Trainee.class)).thenReturn(query);
        when(query.list()).thenReturn(traineeList);

        //when
        List<Trainee> result = dao.findAll();

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(traineeList, result);

        verify(session).createQuery("FROM Trainee", Trainee.class);
        verify(query).list();
    }

    @Test
    @DisplayName("findAll: returns empty list when storage is empty")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        //given
        when(session.createQuery("FROM Trainee", Trainee.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        List<Trainee> result = dao.findAll();

        //then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(session).createQuery("FROM Trainee", Trainee.class);
        verify(query, times(1)).list();
    }

    @Test
    @DisplayName("delete: removes trainee from storage")
    void delete_existingId_shouldRemoveFromStorage() {
        //given
        Trainee trainee = buildTrainee(1L, "John", "Doe");

        //when
        dao.delete(trainee);

        //then
        verify(sessionFactory).getCurrentSession();
        verify(session).remove(trainee);
    }

    @Test
    @DisplayName("existsByUsername: returns true when username exists")
    void existsByUsername_existing_shouldReturnTrue() {
        //given
        Query<Long> longQuery = Mockito.mock(Query.class);
        String username = "John";

        when(session.createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("username", username)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        //when
        boolean result = dao.existsByUsername(username);

        //then
        assertTrue(result);

        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class);
        verify(longQuery).setParameter("username", username);
        verify(longQuery).uniqueResult();
        verifyNoMoreInteractions(longQuery);
    }

    @Test
    @DisplayName("existsByUsername: returns false when username not found")
    void existsByUsername_nonExisting_shouldReturnFalse() {
        //given
        Query<Long> longQuery = Mockito.mock(Query.class);
        String username = "John";

        when(session.createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("username", username)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(0L);

        //when
        boolean result = dao.existsByUsername(username);

        //then
        assertFalse(result);

        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class);
        verify(longQuery).setParameter("username", username);
        verify(longQuery).uniqueResult();
        verifyNoMoreInteractions(longQuery);
    }

    @Test
    @DisplayName("existsByUsername: returns false when query doesn't have results")
    void existsByUsername_queryDoesntHaveResults_shouldReturnFalse() {
        //given
        Query<Long> longQuery = Mockito.mock(Query.class);
        String username = "John";

        when(session.createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("username", username)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(null);

        //when
        boolean result = dao.existsByUsername(username);

        //then
        assertFalse(result);

        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class);
        verify(longQuery).setParameter("username", username);
        verify(longQuery).uniqueResult();
        verifyNoMoreInteractions(longQuery);
    }

    @Test
    @DisplayName("updateTrainers: returns updated trainers list when trainee list updated")
    void updateTrainers_validParameters_shouldReturnTrainerList() {
        //given
        String traineeUsername = "John.Doe";
        List<String> trainersUsernames = List.of("Jane.Smith", "Bob.Jones");

        Trainee trainee = buildTrainee(1L, "John", "Doe");
        Trainer janeTrainer = buildTrainer(2L, "Jane", "Smith");
        Trainer bobTrainer = buildTrainer(3L, "Bob", "Jones");
        Trainer jakeTrainer = buildTrainer(4L, "Jake", "Smith");
        trainee.setTrainers(new HashSet<>(List.of(jakeTrainer)));

        List<Trainer> trainers = List.of(janeTrainer, bobTrainer);
        Query<Trainer> trainerQuery = Mockito.mock(Query.class);
        Query<Trainee> traineeQuery = Mockito.mock(Query.class);

        when(session.createQuery(TRAINEE_TRAINERS_FETCH_HQL, Trainee.class)).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", traineeUsername)).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        when(session.createQuery(TRAINERS_BY_USERNAMES_HQL, Trainer.class)).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("usernames", trainersUsernames)).thenReturn(trainerQuery);
        when(trainerQuery.list()).thenReturn(trainers);

        //when
        List<Trainer> result = dao.updateTrainers(traineeUsername, trainersUsernames);

        //then
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(2, trainee.getTrainers().size());

        verify(sessionFactory, times(1)).getCurrentSession();
        verify(session).createQuery(TRAINEE_TRAINERS_FETCH_HQL, Trainee.class);
        verify(traineeQuery).setParameter("username", traineeUsername);
        verify(traineeQuery).uniqueResultOptional();

        verify(session).createQuery(TRAINERS_BY_USERNAMES_HQL, Trainer.class);
        verify(trainerQuery).setParameter("usernames", trainersUsernames);
        verify(trainerQuery).list();
    }

    @Test
    @DisplayName("updateTrainers: throw EntityNotFoundException when trainee not found")
    void updateTrainers_traineeNotFound_shouldThrowEntityNotFoundException() {
        //given
        String traineeUsername = "John.Doe";
        List<String> trainersUsernames = List.of("Jane.Smith", "Bob.Jones");
        Query<Trainee> traineeQuery = Mockito.mock(Query.class);

        when(session.createQuery(TRAINEE_TRAINERS_FETCH_HQL, Trainee.class)).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", traineeUsername)).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> dao.updateTrainers(traineeUsername, trainersUsernames));

        verify(sessionFactory, times(1)).getCurrentSession();
        verify(session).createQuery(TRAINEE_TRAINERS_FETCH_HQL, Trainee.class);
        verify(traineeQuery).setParameter("username", traineeUsername);
        verify(traineeQuery).uniqueResultOptional();

        verifyNoMoreInteractions(session);
    }

    @Test
    @DisplayName("updateTrainers: throw EntityNotFoundException when trainersUsernames does not exist")
    void updateTrainers_nonExistingTrainerUsernames_shouldThrowEntityNotFoundException() {
        //given
        String traineeUsername = "John.Doe";
        List<String> trainersUsernames = List.of("Jane.Smith", "Bob.Jones");
        Trainee trainee = buildTrainee(1L, "John", "Doe");
        Trainer jakeTrainer = buildTrainer(2L, "Jake", "Smith");
        trainee.setTrainers(Set.of(jakeTrainer));
        Query<Trainer> trainerQuery = Mockito.mock(Query.class);
        Query<Trainee> traineeQuery = Mockito.mock(Query.class);

        when(session.createQuery(TRAINEE_TRAINERS_FETCH_HQL, Trainee.class)).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", traineeUsername)).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        when(session.createQuery(TRAINERS_BY_USERNAMES_HQL, Trainer.class)).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("usernames", trainersUsernames)).thenReturn(trainerQuery);
        when(trainerQuery.list()).thenReturn(List.of());

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> dao.updateTrainers(traineeUsername, trainersUsernames));
        assertEquals(1, trainee.getTrainers().size());

        verify(sessionFactory, times(1)).getCurrentSession();
        verify(session).createQuery(TRAINEE_TRAINERS_FETCH_HQL, Trainee.class);
        verify(traineeQuery).setParameter("username", traineeUsername);
        verify(traineeQuery).uniqueResultOptional();

        verify(session).createQuery(TRAINERS_BY_USERNAMES_HQL, Trainer.class);
        verify(trainerQuery).setParameter("usernames", trainersUsernames);
        verify(trainerQuery).list();
    }
}