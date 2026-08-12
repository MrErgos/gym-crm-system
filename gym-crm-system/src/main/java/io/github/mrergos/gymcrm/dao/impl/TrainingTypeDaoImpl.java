package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TrainingTypeDao;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDaoImpl.class);

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        log.debug("Finding training type by id: {}", id);
        TrainingType result = sessionFactory.getCurrentSession().find(TrainingType.class, id);
        if (result == null) {
            log.warn("Training type not found by id: {}", id);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<TrainingType> findByName(String trainingTypeName) {
        log.debug("Finding training type by name: {}", trainingTypeName);
        Query<TrainingType> query = sessionFactory.getCurrentSession()
                .createQuery("FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class);
        query.setParameter("name", trainingTypeName);
        Optional<TrainingType> result = query.uniqueResultOptional();
        if (result.isEmpty()) {
            log.warn("Training type not found by name: {}", trainingTypeName);
        }
        return result;
    }

    @Override
    public List<TrainingType> findAll() {
        List<TrainingType> result = sessionFactory.getCurrentSession()
                .createQuery("FROM TrainingType", TrainingType.class)
                .list();
        log.debug("Fetching all training types, total: {}", result.size());
        return result;
    }
}