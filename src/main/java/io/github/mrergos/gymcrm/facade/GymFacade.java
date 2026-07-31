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

    public List<TrainingType> getAvailableTrainingTypes() {
        log.debug("Facade: get available training types");
        return trainerService.getAvailableTrainingTypes();
    }

    public Optional<TrainingType> getTrainingTypeById(Long id) {
        log.debug("Facade: get training type by id={}", id);
        return trainerService.getTrainingTypeById(id);
    }

    public Trainee createTraineeProfile(Trainee trainee) {
        log.info("Facade: create trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());
        return traineeService.createTraineeProfile(trainee);
    }

    public Trainee updateTraineeProfile(Trainee trainee) {
        log.info("Facade: update trainee profile, username={}", trainee.getUsername());
        return traineeService.updateTraineeProfile(trainee);
    }

    public void toggleTraineeActive(String traineeUsername) {
        log.info("Facade: toggle trainee active status, username={}", traineeUsername);
        traineeService.toggleActive(traineeUsername);
    }

    public void deleteTraineeProfile(String traineeUsername) {
        log.info("Facade: delete trainee profile, username={}", traineeUsername);
        traineeService.deleteTraineeProfile(traineeUsername);
    }

    public Optional<Trainee> getTraineeProfile(String traineeUsername) {
        log.debug("Facade: get trainee profile, username={}", traineeUsername);
        return traineeService.getTraineeProfile(traineeUsername);
    }

    public List<Trainee> getAllTrainees() {
        log.debug("Facade: get all trainees");
        return traineeService.getAllTrainees();
    }

    public List<Trainer> getTrainersNotAssigned(String traineeUsername) {
        log.debug("Facade: get trainers not assigned to trainee, username={}", traineeUsername);
        return traineeService.getTrainersNotAssigned(traineeUsername);
    }

    public List<Trainer> updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        log.info("Facade: update trainee's trainers list, username={}", traineeUsername);
        return traineeService.updateTraineeTrainers(traineeUsername, trainerUsernames);
    }

    public Trainer createTrainerProfile(String firstName, String lastName, Long specializationId) {
        log.info("Facade: create trainer profile for {} {}", firstName, lastName);
        return trainerService.createTrainerProfile(firstName, lastName, specializationId);
    }

    public Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        log.info("Facade: create trainer profile for {} {}", firstName, lastName);
        return trainerService.createTrainerProfile(firstName, lastName, specialization);
    }

    public Trainer updateTrainerProfile(Trainer trainer) {
        log.info("Facade: update trainer profile, username={}", trainer.getUsername());
        return trainerService.updateTrainerProfile(trainer);
    }

    public void toggleTrainerActive(String trainerUsername) {
        log.info("Facade: toggle trainer active status, username={}", trainerUsername);
        trainerService.toggleActive(trainerUsername);
    }

    public Optional<Trainer> getTrainerProfile(String trainerUsername) {
        log.debug("Facade: get trainer profile, username={}", trainerUsername);
        return trainerService.getTrainerProfile(trainerUsername);
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

    public List<Training> getTraineeTrainings(String traineeUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        log.debug("Facade: get trainee trainings, username={}", traineeUsername);
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(String trainerUsername,
                                              LocalDate fromDate, LocalDate toDate, String traineeName) {
        log.debug("Facade: get trainer trainings, username={}", trainerUsername);
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }

    public void changePassword(String username, String newPassword) {
        log.info("Facade: change password, username={}", username);

        if (traineeService.existsByUsername(username)) {
            traineeService.changePassword(username, newPassword);
        } else {
            trainerService.changePassword(username, newPassword);
        }
    }
}
