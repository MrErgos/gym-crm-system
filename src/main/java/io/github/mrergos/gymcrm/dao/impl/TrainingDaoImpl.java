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
        Training toSave = new Training(training);

        toSave.setId(generateId());
        log.info("Creating new training: '{}' for traineeId={}, trainerId={}",
                toSave.getTrainingName(),
                toSave.getTraineeId(), toSave.getTrainerId());

        storage.put(toSave.getId(), toSave);
        log.debug("Training saved: id={}, name={}", toSave.getId(), toSave.getTrainingName());
        return new Training(toSave);
    }

    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> result = Optional.ofNullable(storage.get(id)).map(Training::new);
        if (result.isEmpty()) {
            log.warn("Training not found by id: {}", id);
        }
        return result;
    }

    @Override
    public List<Training> findAll() {
        log.debug("Fetching all trainings, total: {}", storage.size());
        return storage.values().stream()
                .map(Training::new)
                .toList();
    }

    private Long generateId() {
        return storage.isEmpty() ? 1L : Collections.max(storage.keySet()) + 1;
    }
}
