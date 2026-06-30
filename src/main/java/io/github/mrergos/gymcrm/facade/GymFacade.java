package io.github.mrergos.gymcrm.facade;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.service.TraineeService;
import io.github.mrergos.gymcrm.service.TrainerService;
import io.github.mrergos.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        log.debug("GymFacade initialized with all three services");
    }

    public Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Facade: create trainee profile for {} {}",firstName,lastName);

        return traineeService.createTraineeProfile(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTraineeProfile(Trainee trainee) {
        log.info("Facade: update trainee profile, id={}",trainee.getUserId());

        return traineeService.updateTraineeProfile(trainee);
    }

    public void deleteTraineeProfile(Long id) {
        log.info("Facade: delete trainee profile, id={}",id);
        traineeService.deleteTraineeProfile(id);
    }

    public Optional<Trainee> getTraineeProfile(Long id) {
        log.debug("Facade: get trainee profile, id={}", id);
        return traineeService.getTraineeProfile(id);
    }

    public List<Trainee> getAllTrainees() {
        log.debug("Facade: get all trainees");
        return traineeService.getAllTrainees();
    }


    public Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        log.info("Facade: create trainer profile for {} {}", firstName, lastName);
        return trainerService.createTrainerProfile(firstName, lastName, specialization);
    }

    public Trainer updateTrainerProfile(Trainer trainer) {
        log.info("Facade: update trainer profile, id={}", trainer.getUserId());
        return trainerService.updateTrainerProfile(trainer);
    }

    public Optional<Trainer> getTrainerProfile(Long id) {
        log.debug("Facade: get trainer profile, id={}", id);
        return trainerService.getTrainerProfile(id);
    }

    public List<Trainer> getAllTrainers() {
        log.debug("Facade: get all trainers");
        return trainerService.getAllTrainers();
    }


    public Training createTraining(Training training) {
        log.info("Facade: create training '{}'", training.getTrainingName());
        return trainingService.createTraining(training);
    }

    public Optional<Training> getTraining(Long id) {
        log.debug("Facade: get training, id={}", id);
        return trainingService.getTraining(id);
    }

    public List<Training> getAllTrainings() {
        log.debug("Facade: get all trainings");
        return trainingService.getAllTrainings();
    }
}
