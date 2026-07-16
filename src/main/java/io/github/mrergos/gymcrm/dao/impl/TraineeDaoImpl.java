package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee.getId() == null) {
            sessionFactory.getCurrentSession().persist(trainee);
            log.info("Creating new trainee with username: {}", trainee.getUsername());
        } else {
            trainee = sessionFactory.getCurrentSession().merge(trainee);
            log.info("Updating trainee with id: {}", trainee.getId());
        }

        log.debug("Trainee saved: id={}, username={}", trainee.getId(), trainee.getUsername());
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        Query<Trainee> query = sessionFactory.getCurrentSession()
                .createQuery("FROM Trainee t " +
                                "LEFT JOIN FETCH t.trainers tr" +
                                " LEFT JOIN FETCH tr.specialization" +
                                " WHERE t.id = :id",
                        Trainee.class);
        query.setParameter("id", id);
        Optional<Trainee> result = query.uniqueResultOptional();
        if (result.isEmpty()) {
            log.warn("Trainee not found by id: {}", id);
        }
        return result;
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        Query<Trainee> query = sessionFactory.getCurrentSession()
                .createQuery("FROM Trainee t " +
                        "LEFT JOIN FETCH t.trainers tr" +
                        " LEFT JOIN FETCH tr.specialization" +
                        " WHERE t.username = :username", Trainee.class);
        query.setParameter("username", username);
        Optional<Trainee> result = query.uniqueResultOptional();
        if (result.isEmpty()) {
            log.warn("Trainee not found by username: {}", username);
        }
        return result;
    }

    @Override
    public List<Trainee> findAll() {
        List<Trainee> result = sessionFactory.getCurrentSession()
                .createQuery("FROM Trainee", Trainee.class)
                .list();
        log.debug("Fetching all trainees, total: {}", result.size());
        return result;
    }

    @Override
    public void delete(Trainee trainee) {
        sessionFactory.getCurrentSession()
                .remove(trainee);
        log.info("Deleted trainee with id: {}", trainee.getId());
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = sessionFactory.getCurrentSession()
                        .createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class)
                .setParameter("username", username)
                .uniqueResult();
        boolean exists = count != null && count > 0;
        log.debug("Username '{}' exists in trainees: {}", username, exists);
        return exists;
    }

    @Override
    public List<Trainer> updateTrainers(String traineeUsername, List<String> trainerUsernames) {
        Session session = sessionFactory.getCurrentSession();

        Trainee trainee = session
                .createQuery("FROM Trainee t LEFT JOIN FETCH t.trainers WHERE t.username = :username",
                        Trainee.class)
                .setParameter("username", traineeUsername)
                .uniqueResultOptional()
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + traineeUsername));

        List<Trainer> trainers = session
                .createQuery("FROM Trainer t JOIN FETCH t.specialization WHERE t.username IN :usernames",
                        Trainer.class)
                .setParameter("usernames", trainerUsernames)
                .list();

        if (trainers.size() != trainerUsernames.size()) {
            log.warn("Some trainer usernames not found while updating trainee's trainers, trainee={}", traineeUsername);
            throw new EntityNotFoundException("One or more trainers not found");
        }

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        log.info("Updated trainers list for trainee, username={}, count={}", traineeUsername, trainers.size());
        return trainers;
    }
}
