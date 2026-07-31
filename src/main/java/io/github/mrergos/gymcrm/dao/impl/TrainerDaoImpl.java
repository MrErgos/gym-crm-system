package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainer;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainer save(Trainer trainer) {
        if (trainer.getId() == null) {
            sessionFactory.getCurrentSession().persist(trainer);
            log.info("Creating new trainer with username: {}", trainer.getUsername());
        } else {
            trainer = sessionFactory.getCurrentSession().merge(trainer);
            log.info("Updating trainer with id: {}", trainer.getId());
        }
        log.debug("Trainer saved: id={}, username={}", trainer.getId(), trainer.getUsername());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        Query<Trainer> query = sessionFactory.getCurrentSession()
                .createQuery("FROM Trainer t JOIN FETCH t.specialization LEFT JOIN FETCH t.trainees WHERE t.id = :id",
                        Trainer.class);
        query.setParameter("id", id);
        Optional<Trainer> result = query.uniqueResultOptional();
        if (result.isEmpty()) {
            log.warn("Trainer not found by id: {}", id);
        }
        return result;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        Query<Trainer> query = sessionFactory.getCurrentSession()
                .createQuery("FROM Trainer t JOIN FETCH t.specialization LEFT JOIN FETCH t.trainees " +
                        "WHERE t.username = :username", Trainer.class);
        query.setParameter("username", username);
        Optional<Trainer> result = query.uniqueResultOptional();
        if (result.isEmpty()) {
            log.warn("Trainer not found by username: {}", username);
        }
        return result;
    }

    @Override
    public List<Trainer> findAll() {
        List<Trainer> result = sessionFactory.getCurrentSession()
                        .createQuery("FROM Trainer t JOIN FETCH t.specialization", Trainer.class)
                                .list();
        log.debug("Fetching all trainers, total: {}", result.size());
        return result;
    }

    @Override
    public List<Trainer> findAllNotAssignedToTrainee(String traineeUsername) {
        log.debug("Finding trainers not assigned to trainee: {}", traineeUsername);
        String queryString = """
            FROM Trainer tr JOIN FETCH tr.specialization
            WHERE tr NOT IN (
                SELECT tt FROM Trainee t JOIN t.trainers tt
                WHERE t.username = :traineeUsername
            )
            """;
        List<Trainer> result = sessionFactory.getCurrentSession()
                .createQuery(queryString, Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .list();
        log.debug("Found {} trainers not assigned to trainee {}", result.size(), traineeUsername);
        return result;
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.username = :username", Long.class)
                .setParameter("username", username)
                .uniqueResult();
        boolean exists = count != null && count > 0;
        log.debug("Username '{}' exists in trainers: {}", username, exists);
        return exists;
    }
}
