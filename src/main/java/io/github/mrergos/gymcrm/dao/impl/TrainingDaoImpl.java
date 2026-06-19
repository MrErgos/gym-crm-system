package io.github.mrergos.gymcrm.dao.impl;

import io.github.mrergos.gymcrm.dao.TrainingDao;
import io.github.mrergos.gymcrm.entity.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private Map<Long, Training> storage;

    @Autowired
    @Qualifier("trainingStorage")
    public void setStorage(Map<Long, Training> storage) {
        this.storage = storage;
    }

    @Override
    public Training save(Training training) {
        if (training.getId() == null) {
            training.setId(generateId());
            log.info("Creating new training: '{}' for traineeId={}, trainerId={}",
                    training.getTrainingName(),
                    training.getTraineeId(), training.getTrainerId());
        } else {
            log.info("Updating training with id: {}", training.getId());
        }
        storage.put(training.getId(), training);
        log.debug("Training saved: id={}, name={}", training.getId(), training.getTrainingName());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> result = Optional.ofNullable(storage.get(id));
        if (result.isEmpty()) {
            log.warn("Training not found by id: {}", id);
        }
        return result;
    }

    @Override
    public List<Training> findAll() {
        log.debug("Fetching all trainings, total: {}", storage.size());
        return new ArrayList<>(storage.values());
    }

    private Long generateId() {
        return storage.isEmpty() ? 1L : Collections.max(storage.keySet()) + 1;
    }
}
