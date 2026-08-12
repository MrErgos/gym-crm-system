package io.github.mrergos.gymcrm.dao.impl;

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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingTypeDaoImpl tests")
class TrainingTypeDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TrainingType> query;

    @InjectMocks
    private TrainingTypeDaoImpl dao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    private TrainingType buildTrainingType(Long id, String name) {
        return new TrainingType(id, name);
    }

    @Test
    @DisplayName("findById: returns training type when found")
    void findById_existingId_shouldReturnTrainingType() {
        //given
        TrainingType trainingType = buildTrainingType(1L, "Yoga");

        when(session.find(TrainingType.class, 1L)).thenReturn(trainingType);

        //when
        Optional<TrainingType> result = dao.findById(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals("Yoga", result.get().getTrainingTypeName());
        verify(session).find(TrainingType.class, 1L);
    }

    @Test
    @DisplayName("findById: returns empty when not found")
    void findById_nonExistingId_shouldReturnEmpty() {
        //given
        when(session.find(TrainingType.class, 999L)).thenReturn(null);

        //when
        Optional<TrainingType> result = dao.findById(999L);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByName: returns training type when found")
    void findByName_existingName_shouldReturnTrainingType() {
        //given
        TrainingType trainingType = buildTrainingType(1L, "Yoga");

        when(session.createQuery("FROM TrainingType t WHERE t.trainingTypeName = :name",
                TrainingType.class))
                .thenReturn(query);
        when(query.setParameter("name", "Yoga")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainingType));

        //when
        Optional<TrainingType> result = dao.findByName("Yoga");

        //then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(query).setParameter("name", "Yoga");
    }

    @Test
    @DisplayName("findByName: returns empty when not found")
    void findByName_nonExistingName_shouldReturnEmpty() {
        //given
        when(session.createQuery(eq("FROM TrainingType t WHERE t.trainingTypeName = :name"), eq(TrainingType.class)))
                .thenReturn(query);
        when(query.setParameter("name", "unknown")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        //when
        Optional<TrainingType> result = dao.findByName("unknown");

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll: returns all stored training types")
    void findAll_shouldReturnAll() {
        //given
        TrainingType t1 = buildTrainingType(1L, "Yoga");
        TrainingType t2 = buildTrainingType(2L, "Cardio");

        when(session.createQuery("FROM TrainingType", TrainingType.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of(t1, t2));

        //when
        List<TrainingType> all = dao.findAll();

        //then
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll: returns empty list when nothing stored")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        //given
        when(session.createQuery("FROM TrainingType", TrainingType.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        //when
        List<TrainingType> all = dao.findAll();

        //then
        assertTrue(all.isEmpty());
    }
}
