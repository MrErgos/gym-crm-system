package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerDaoImpl tests")
class TrainerDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainer> query;

    @InjectMocks
    private TrainerDaoImpl dao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    private Trainer buildTrainer(Long id, String firstName, String lastName) {
        Trainer t = new Trainer();
        t.setId(id);
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setUsername(firstName + "." + lastName);
        t.setPassword("password123");
        t.setActive(true);
        t.setSpecialization(new TrainingType(1L, "Fitness"));
        return t;
    }

    @Test
    @DisplayName("save: return same trainer when persist trainer")
    void save_newTrainer_shouldCallPersist() {
        //given
        Trainer trainer = buildTrainer(null, "Jane", "Smith");

        //when
        Trainer saved = dao.save(trainer);

        //then
        assertSame(saved, trainer);
        verify(session).persist(trainer);
        verifyNoMoreInteractions(session);
    }

    @Test
    @DisplayName("save: return trainer when call merge")
    void save_existingTrainer_shouldCallMerge() {
        //given
        Trainer trainer = buildTrainer(5L, "Jane", "Smith");
        when(session.merge(trainer)).thenReturn(trainer);

        //when
        dao.save(trainer);

        //then
        verify(session).merge(trainer);
        verifyNoMoreInteractions(session);
    }

    @Test
    @DisplayName("findById: returns trainer when exists")
    void findById_existingId_shouldReturnTrainer() {
        //given
        Long trainerId = 1L;
        Trainer saved = buildTrainer(trainerId, "Jane", "Smith");

        when(session.createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.id = :id",
                Trainer.class)).thenReturn(query);
        when(query.setParameter("id", trainerId)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(saved));

        //when
        Optional<Trainer> result = dao.findById(trainerId);

        //then
        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());

        verify(session).createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.id = :id",
                Trainer.class);
        verify(query).setParameter("id", trainerId);
    }

    @Test
    @DisplayName("findById: returns empty when not found")
    void findById_nonExistingId_shouldReturnEmpty() {
        //given
        Long trainerId = 999L;

        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter("id", trainerId)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        Optional<Trainer> result = dao.findById(trainerId);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUsername: returns trainer when exists")
    void findByUsername_existingUsername_shouldReturnTrainer() {
        //given
        String username = "Jane.Smith";
        Trainer saved = buildTrainer(1L, "Jane", "Smith");

        when(session.createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.username = :username",
                Trainer.class))
                .thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(saved));

        //when
        Optional<Trainer> result = dao.findByUsername(username);

        //then
        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());

        verify(session).createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.username = :username",
                Trainer.class);
        verify(query).setParameter("username", username);
        verify(query).uniqueResultOptional();
        verifyNoMoreInteractions(query);

    }

    @Test
    @DisplayName("findByUsername: returns empty when not found")
    void findByUsername_nonExistingUsername_shouldReturnEmpty() {
        //given
        String username = "Jane.Smith";

        when(session.createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.username = :username",
                Trainer.class))
                .thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        Optional<Trainer> result = dao.findByUsername(username);

        //then
        assertTrue(result.isEmpty());

        verify(session).createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.username = :username",
                Trainer.class);
        verify(query).setParameter("username", username);
        verify(query).uniqueResultOptional();
        verifyNoMoreInteractions(query);

    }
    
    @Test
    @DisplayName("findAll: returns all stored trainers")
    void findAll_shouldReturnAllTrainers() {
        //given
        List<Trainer> trainerList = List.of(buildTrainer(null, "Jane", "Smith"),
                buildTrainer(null, "Bob", "Jones"));

        when(session.createQuery("FROM Trainer t JOIN FETCH t.specialization", Trainer.class))
                .thenReturn(query);
        when(query.list()).thenReturn(trainerList);

        //when
        List<Trainer> result = dao.findAll();

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(trainerList, result);

        verify(session).createQuery("FROM Trainer t JOIN FETCH t.specialization", Trainer.class);
        verify(query).list();
    }

    @Test
    @DisplayName("findAll: returns empty list when storage is empty")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        //given
        when(session.createQuery("FROM Trainer t JOIN FETCH t.specialization", Trainer.class))
                .thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        List<Trainer> result = dao.findAll();

        //then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(session).createQuery("FROM Trainer t JOIN FETCH t.specialization", Trainer.class);
        verify(query, times(1)).list();
    }

    @Test
    @DisplayName("findAllNotAssignedToTrainee: ")
    void findAllNotAssignedToTrainee_validUsername_shouldReturnEmptyList() {
        //given
        String traineeUsername = "John.Doe";
        String queryString = """
            FROM Trainer tr JOIN FETCH tr.specialization
            WHERE tr NOT IN (
                SELECT tt FROM Trainee t JOIN t.trainers tt
                WHERE t.username = :traineeUsername
            )
            """;
        Trainer trainer = buildTrainer(2L, "Jane", "Smith");

        when(session.createQuery(queryString, Trainer.class)).thenReturn(query);
        when(query.setParameter("traineeUsername", traineeUsername)).thenReturn(query);
        when(query.list()).thenReturn(List.of(trainer));

        //when
        List<Trainer> result = dao.findAllNotAssignedToTrainee(traineeUsername);

        //then
        assertNotNull(result);
        assertEquals(1, result.size());

        assertSame(trainer, result.get(0));
        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery(queryString, Trainer.class);
        verify(query).setParameter("traineeUsername", traineeUsername);
        verify(query).list();
    }

    @Test
    @DisplayName("existsByUsername: returns true when username exists")
    void existsByUsername_existing_shouldReturnTrue() {
        //given
        Query<Long> longQuery = Mockito.mock(Query.class);
        String username = "Jane.Smith";

        when(session.createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("username", username)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        //when
        boolean result = dao.existsByUsername(username);

        //then
        assertTrue(result);

        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                Long.class);
        verify(longQuery).setParameter("username", username);
        verify(longQuery).uniqueResult();
        verifyNoMoreInteractions(longQuery);
    }

    @Test
    @DisplayName("existsByUsername: returns false when username not found")
    void existsByUsername_nonExisting_shouldReturnFalse() {
        //given
        Query<Long> longQuery = Mockito.mock(Query.class);
        String username = "Jane.Smith";

        when(session.createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("username", username)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(0L);

        //when
        boolean result = dao.existsByUsername(username);

        //then
        assertFalse(result);

        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                Long.class);
        verify(longQuery).setParameter("username", username);
        verify(longQuery).uniqueResult();
        verifyNoMoreInteractions(longQuery);
    }

    @Test
    @DisplayName("existsByUsername: returns false when query doesn't have results")
    void existsByUsername_queryDoesntHaveResults_shouldReturnFalse() {
        //given
        Query<Long> longQuery = Mockito.mock(Query.class);
        String username = "Jane.Smith";

        when(session.createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("username", username)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(null);

        //when
        boolean result = dao.existsByUsername(username);

        //then
        assertFalse(result);

        verify(sessionFactory).getCurrentSession();
        verify(session).createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                Long.class);
        verify(longQuery).setParameter("username", username);
        verify(longQuery).uniqueResult();
        verifyNoMoreInteractions(longQuery);
    }
}