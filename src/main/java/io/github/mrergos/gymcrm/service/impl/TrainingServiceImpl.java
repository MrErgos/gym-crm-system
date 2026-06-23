package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.dao.TrainingDao;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }


    @Override
    public Training createTraining(Training training) {
        Assert.notNull(training, "Training must not be null");
        Assert.isNull(training.getId(), "Training ID must be null");
        Assert.notNull(training.getTraineeId(), "Trainee id must not be null");
        Assert.notNull(training.getTrainerId(), "Trainer id must not be null");
        Assert.hasText(training.getTrainingName(), "Training name must not be blank");

        traineeDao.findById(training.getTraineeId())
                .orElseThrow(() -> {
                    log.warn("Attempt to create training for non-existing trainee, id={}", training.getTraineeId());
                    return new EntityNotFoundException("Trainee not found with id: " + training.getTraineeId());
                });

        trainerDao.findById(training.getTrainerId())
                .orElseThrow(() -> {
                    log.warn("Attempt to create training for non-existing trainer, id={}", training.getTrainerId());
                    return new EntityNotFoundException("Trainer not found with id: " + training.getTrainerId());
                });

        log.info("Creating training '{}' for traineeId={}, trainerId={}",
                training.getTrainingName(), training.getTraineeId(), training.getTrainerId());

        Training saved = trainingDao.save(training);
        log.info("Training created: id={}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Training> getTraining(Long id) {
        log.debug("Fetching training, id={}", id);
        return trainingDao.findById(id);
    }

    @Override
    public List<Training> getAllTrainings() {
        log.debug("Fetching all trainings");
        return trainingDao.findAll();
    }
}
