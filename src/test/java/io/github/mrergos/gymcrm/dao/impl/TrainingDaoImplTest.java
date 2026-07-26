package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingDaoImpl tests")
class TrainingDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Training> query;

    @InjectMocks
    private TrainingDaoImpl dao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    private Training buildTraining(Long id, Trainee trainee, Trainer trainer) {
        Training t = new Training();
        t.setId(id);
        t.setTrainee(trainee);
        t.setTrainer(trainer);
        t.setTrainingName("Test Training");
        t.setTrainingType(new TrainingType(1L, "Yoga"));
        t.setTrainingDate(LocalDate.of(2024, 6, 1));
        t.setTrainingDuration(60);
        return t;
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
        t.setSpecialization(new TrainingType(1L,"Yoga"));
        return t;
    }
    
    @Test
    @DisplayName("save: persists training via current session and returns it")
    void save_shouldPersistAndReturnTraining() {
        //given
        
        Training training = buildTraining(null, buildTrainee(1L, "John", "Doe"),
                buildTrainer(2L, "Jane", "Smith"));

        //when
        Training result = dao.save(training);

        //then
        verify(session).persist(training);
        assertEquals(training, result);
    }

    @Test
    @DisplayName("save: persists training via current session and returns it when trainee and trainer are null")
    void save_traineeAndTrainerAreNull_shouldPersistAndReturnTraining() {
        //given
        
        Training training = buildTraining(null, null,
                null);

        //when
        Training result = dao.save(training);

        //then
        verify(session).persist(training);
        assertEquals(training, result);
    }

    @Test
    @DisplayName("findById: returns training when found")
    void findById_existingId_shouldReturnTraining() {
        //given
        Training training = buildTraining(1L, buildTrainee(1L, "John", "Doe"),
                buildTrainer(2L, "Jane", "Smith"));

        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter("id", 1L)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(training));

        //when
        Optional<Training> result = dao.findById(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("Test Training", result.get().getTrainingName());
        verify(query).setParameter("id", 1L);
    }

    @Test
    @DisplayName("findById: returns empty when not found")
    void findById_nonExistingId_shouldReturnEmpty() {
        //given
        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter("id", 999L)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        Optional<Training> result = dao.findById(999L);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll: returns all stored trainings")
    void findAll_shouldReturnAll() {
        //given
        Training t1 = buildTraining(1L, buildTrainee(1L,  "John", "Doe"),
                buildTrainer(2L,  "Jane", "Smith"));
        Training t2 = buildTraining(2L, buildTrainee(3L, "Alice", "Smith"),
                buildTrainer(4L, "Bob", "Jones"));

        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of(t1, t2));

        //when
        List<Training> all = dao.findAll();

        //then
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll: returns empty list when nothing stored")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        //given
        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        List<Training> all = dao.findAll();

        //then
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("findTraineeTrainings: builds query with only mandatory parameter when optional filters are null")
    void findTraineeTrainings_onlyMandatoryParam_shouldNotAddOptionalFilters() {
        //given
        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        dao.findTraineeTrainings( "John.Doe",null, null, null, null);

        //then
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(queryCaptor.capture(), eq(Training.class));
        String hql = queryCaptor.getValue();

        assertTrue(hql.contains("t.trainee.username = :traineeUsername"));
        assertFalse(hql.contains(":fromDate"));
        assertFalse(hql.contains(":toDate"));
        assertFalse(hql.contains(":trainerName"));
        assertFalse(hql.contains(":trainingTypeName"));

        verify(query).setParameter("traineeUsername", "John.Doe");
        verify(query, never()).setParameter(eq("fromDate"), any());
        verify(query, never()).setParameter(eq("toDate"), any());
        verify(query, never()).setParameter(eq("trainerName"), any());
        verify(query, never()).setParameter(eq("trainingTypeName"), any());
    }

    @Test
    @DisplayName("findTraineeTrainings: builds query with all optional filters when provided")
    void findTraineeTrainings_allFilters_shouldAddAllParams() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        dao.findTraineeTrainings( "John.Doe", from, to, "Jane Smith", "Yoga");

        //then
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(queryCaptor.capture(), eq(Training.class));
        String hql = queryCaptor.getValue();

        assertTrue(hql.contains("t.trainee.username = :traineeUsername"));
        assertTrue(hql.contains("t.trainingDate >= :fromDate"));
        assertTrue(hql.contains("t.trainingDate <= :toDate"));
        assertTrue(hql.contains("concat(t.trainer.firstName, ' ', t.trainer.lastName) = :trainerName"));
        assertTrue(hql.contains("t.trainingType.trainingTypeName = :trainingTypeName"));

        verify(query).setParameter("traineeUsername", "John.Doe");
        verify(query).setParameter("fromDate", from);
        verify(query).setParameter("toDate", to);
        verify(query).setParameter("trainerName", "Jane Smith");
        verify(query).setParameter("trainingTypeName", "Yoga");
    }

    @Test
    @DisplayName("findTraineeTrainings: returns trainings from query result")
    void findTraineeTrainings_shouldReturnQueryResult() {
        //given
        Training training = buildTraining(1L, buildTrainee(1L,  "John", "Doe"),
                buildTrainer(2L,  "Jane", "Smith"));

        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(training));

        //when
        List<Training> result = dao.findTraineeTrainings( "John.Doe", null, null, null, null);

        //then
        assertEquals(1, result.size());
        assertEquals(training, result.get(0));
    }

    @Test
    @DisplayName("findTrainerTrainings: builds query with only mandatory parameter when optional filters are null")
    void findTrainerTrainings_onlyMandatoryParam_shouldNotAddOptionalFilters() {
        //given
        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        dao.findTrainerTrainings( "Jane.Smith", null, null, null);

        //then
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(queryCaptor.capture(), eq(Training.class));
        String hql = queryCaptor.getValue();

        assertTrue(hql.contains("t.trainer.username = :trainerUsername"));
        assertFalse(hql.contains(":fromDate"));
        assertFalse(hql.contains(":toDate"));
        assertFalse(hql.contains(":traineeName"));

        verify(query).setParameter("trainerUsername", "Jane.Smith");
        verify(query, never()).setParameter(eq("fromDate"), any());
        verify(query, never()).setParameter(eq("toDate"), any());
        verify(query, never()).setParameter(eq("traineeName"), any());
    }

    @Test
    @DisplayName("findTrainerTrainings: builds query with all optional filters when provided")
    void findTrainerTrainings_allFilters_shouldAddAllParams() {
        //given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        dao.findTrainerTrainings( "Jane.Smith" ,from, to, "John Doe");

        //then
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(queryCaptor.capture(), eq(Training.class));
        String hql = queryCaptor.getValue();

        assertTrue(hql.contains("t.trainer.username = :trainerUsername"));
        assertTrue(hql.contains("t.trainingDate >= :fromDate"));
        assertTrue(hql.contains("t.trainingDate <= :toDate"));
        assertTrue(hql.contains("concat(t.trainee.firstName, ' ', t.trainee.lastName) = :traineeName"));

        verify(query).setParameter("trainerUsername", "Jane.Smith");
        verify(query).setParameter("fromDate", from);
        verify(query).setParameter("toDate", to);
        verify(query).setParameter("traineeName", "John Doe");
    }

    @Test
    @DisplayName("findTrainerTrainings: returns trainings from query result")
    void findTrainerTrainings_shouldReturnQueryResult() {
        //given
        Training training = buildTraining(1L, buildTrainee(1L,  "John", "Doe"),
                buildTrainer(2L,  "Jane", "Smith"));

        
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(training));

        //when
        List<Training> result = dao.findTrainerTrainings( "Jane.Smith", null, null, null);

        //then
        assertEquals(1, result.size());
        assertEquals(training, result.get(0));
    }
}
