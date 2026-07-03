package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TrainingDao;
import io.github.mrergos.gymcrm.entity.Training;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Training save(Training training) {

        sessionFactory.getCurrentSession().persist(training);
        log.info("Creating new training: '{}' for traineeId={}, trainerId={}",
                training.getTrainingName(),
                training.getTrainee() != null ? training.getTrainee().getId() : null,
                training.getTrainer() != null ? training.getTrainer().getId() : null);

        log.debug("Training saved: id={}, name={}", training.getId(), training.getTrainingName());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Training result = sessionFactory.getCurrentSession()
                .find(Training.class, id);
        if (result == null) {
            log.warn("Training not found by id: {}", id);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public List<Training> findAll() {
        List<Training> result = sessionFactory.getCurrentSession()
                        .createQuery("FROM Training", Training.class)
                .list();
        log.debug("Fetching all trainings, total: {}", result.size());
        return result;
    }

    @Override
    public List<Training> findTraineeTrainings(String traineeUsername,
                                               LocalDate fromDate, LocalDate toDate,
                                               String trainerName, String trainingTypeName) {
        log.debug("Finding trainee training by criteria");
        StringBuilder queryString = new StringBuilder(
                "FROM Training t WHERE t.trainee.username = :traineeUsername"
        );
        Map<String, Object> params = new HashMap<>();
        params.put("traineeUsername", traineeUsername);

        if (fromDate != null) {
            queryString.append(" AND t.trainingDate >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            queryString.append(" AND t.trainingDate <= :toDate");
            params.put("toDate", toDate);
        }
        if (trainerName != null) {
            queryString.append(" AND concat(t.trainer.firstName, ' ', t.trainer.lastName) = :trainerName");
            params.put("trainerName", trainerName);
        }
        if (trainingTypeName != null) {
            queryString.append(" AND t.trainingType.trainingTypeName = :trainingTypeName");
            params.put("trainingTypeName", trainingTypeName);
        }

        Query<Training> query = sessionFactory.getCurrentSession()
                .createQuery(queryString.toString(), Training.class);
        params.forEach(query::setParameter);

        List<Training> result = query.list();
        log.debug("Found {} trainings for trainee {}", result.size(), traineeUsername);
        return result;
    }

    @Override
    public List<Training> findTrainerTrainings(String trainerUsername,
                                               LocalDate fromDate, LocalDate toDate,
                                               String traineeName) {
        StringBuilder queryString = new StringBuilder(
                "FROM Training t WHERE t.trainer.username = :trainerUsername");
        Map<String, Object> params = new HashMap<>();
        params.put("trainerUsername", trainerUsername);

        if (fromDate != null) {
            queryString.append(" AND t.trainingDate >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            queryString.append(" AND t.trainingDate <= :toDate");
            params.put("toDate", toDate);
        }
        if (traineeName != null) {
            queryString.append(" AND concat(t.trainee.firstName, ' ', t.trainee.lastName) = :traineeName");
            params.put("traineeName", traineeName);
        }

        Query<Training> query = sessionFactory.getCurrentSession().createQuery(queryString.toString(), Training.class);
        params.forEach(query::setParameter);

        List<Training> result = query.list();
        log.debug("Found {} trainings for trainer {}", result.size(), trainerUsername);
        return result;
    }
}
