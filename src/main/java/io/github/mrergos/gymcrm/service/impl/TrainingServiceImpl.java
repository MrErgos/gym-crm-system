package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.dao.TrainingDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
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
    @Transactional
    public Training createTraining(Training training) {
        Assert.notNull(training, "Training must not be null");
        Assert.isNull(training.getId(), "Training ID must be null");
        Assert.notNull(training.getTrainee(), "Trainee must not be null");
        Assert.notNull(training.getTrainer(), "Trainer must not be null");
        Assert.hasText(training.getTrainingName(), "Training name must not be blank");
        Assert.notNull(training.getTrainingType(), "Training type must not be null");
        Assert.notNull(training.getTrainingDate(), "Training date must not be null");
        Assert.notNull(training.getTrainingDuration(), "Training duration must not be null");

        Trainee trainee = traineeDao.findById(training.getTrainee().getId())
                .orElseThrow(() -> {
                    log.warn("Attempt to create training for non-existing trainee, id={}",
                            training.getTrainee().getId());
                    return new EntityNotFoundException("Trainee not found with id: " + training.getTrainee().getId());
                });

        Trainer trainer = trainerDao.findById(training.getTrainer().getId())
                .orElseThrow(() -> {
                    log.warn("Attempt to create training for non-existing trainer, id={}",
                            training.getTrainer().getId());
                    return new EntityNotFoundException("Trainer not found with id: " + training.getTrainer().getId());
                });

        training.setTrainee(trainee);
        training.setTrainer(trainer);

        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            log.info("Trainer {} auto-assigned to trainee {} trainers list via new training",
                    trainer.getUsername(), trainee.getUsername());
        }

        log.info("Creating training '{}' for traineeUsername={}, trainerUsername={}",
                training.getTrainingName(), trainee.getUsername(), trainer.getUsername());

        Training saved = trainingDao.save(training);
        log.info("Training created: id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> getTraining(Long id) {
        log.debug("Fetching training, id={}", id);
        return trainingDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        log.debug("Fetching all trainings");
        return trainingDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        Assert.hasText(traineeUsername, "Trainee username must not be blank");
        log.debug("Fetching trainee trainings, username={}, from={}, to={}, trainer={}, type={}",
                traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
        return trainingDao.findTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        Assert.hasText(trainerUsername, "Trainer username must not be blank");
        log.debug("Fetching trainer trainings, username={}, from={}, to={}, trainee={}",
                trainerUsername, fromDate, toDate, traineeName);
        return trainingDao.findTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }
}
