package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private Map<Long, Trainee> storage;

    @Autowired
    @Qualifier("traineeStorage")
    public void setStorage(Map<Long, Trainee> storage) {
        this.storage = storage;
    }

    @Override
    public Trainee save(Trainee trainee) {
        Trainee traineeDbCopy = new Trainee(trainee);

        if (traineeDbCopy.getUserId() == null) {
            traineeDbCopy.setUserId(generateId());
            log.info("Creating new trainee with username: {}", traineeDbCopy.getUsername());
        } else {
            log.info("Updating trainee with id: {}", traineeDbCopy.getUserId());
        }

        storage.put(traineeDbCopy.getUserId(), traineeDbCopy);
        log.debug("Trainee saved: id={}, username={}", traineeDbCopy.getUserId(), traineeDbCopy.getUsername());
        return new Trainee(traineeDbCopy);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        Optional<Trainee> result = Optional.ofNullable(storage.get(id)).map(Trainee::new);
        if (result.isEmpty()) {
            log.warn("Trainee not found by id: {}", id);
        }
        return result;
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Fetching all trainees, total: {}", storage.size());
        return storage.values().stream()
                .map(Trainee::new)
                .toList();
    }

    @Override
    public void delete(Long id) {
        Trainee removed = storage.remove(id);
        if (removed != null) {
            log.info("Deleted trainee with id: {}", id);
        } else {
            log.warn("Trainee not found for deletion, id: {}", id);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        boolean exists = storage.values().stream()
                .anyMatch(trainee -> trainee.getUsername().equals(username));
        log.debug("Username '{}' exists in trainees: {}", username, exists);
        return exists;
    }

    private Long generateId() {
        return storage.isEmpty() ? 1L : Collections.max(storage.keySet()) + 1;
    }
}
