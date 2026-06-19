package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private Map<Long, Trainer> storage;

    @Autowired
    @Qualifier("trainerStorage")
    public void setStorage(Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    @Override
    public Trainer save(Trainer trainer) {
        if (trainer.getUserId() == null) {
            trainer.setUserId(generateId());
            log.info("Creating new trainer with username: {}", trainer.getUsername());
        } else {
            log.info("Updating trainer with id: {}", trainer.getUserId());
        }
        storage.put(trainer.getUserId(), trainer);
        log.debug("Trainer saved: id={}, username={}", trainer.getUserId(), trainer.getUsername());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        Optional<Trainer> result = Optional.ofNullable(storage.get(id));
        if (result.isEmpty()) {
            log.warn("Trainer not found by id: {}", id);
        }
        return result;
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Fetching all trainers, total: {}", storage.size());
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsByUsername(String username) {
        boolean exists = storage.values().stream()
                .anyMatch(trainer -> trainer.getUsername().equals(username));
        log.debug("Username '{}' exists in trainers: {}", username, exists);
        return exists;
    }

    private Long generateId() {
        return storage.isEmpty() ? 1L : Collections.max(storage.keySet()) + 1;
    }
}
