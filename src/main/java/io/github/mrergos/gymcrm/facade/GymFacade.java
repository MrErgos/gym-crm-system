package io.github.mrergos.gymcrm.facade;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.service.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    @Autowired
    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authenticationService = authenticationService;
        log.debug("GymFacade initialized with all three services");
    }

    private void authenticate(Credentials credentials) {
        authenticationService.authenticate(credentials.username(), credentials.password());
    }

    public void login(Credentials credentials) {
        log.info("Facade: login username={}", credentials.username());
        authenticate(credentials);
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
        log.info("Facade: create trainee profile for {} {}",trainee.getFirstName(),trainee.getLastName());

        return traineeService.createTraineeProfile(trainee);
    }

    public Trainee updateTraineeProfile(Credentials credentials, Trainee trainee) {
        authenticate(credentials);
        log.info("Facade: update trainee profile, username={}", trainee.getUsername());
        return traineeService.updateTraineeProfile(trainee);
    }

    public void changeTraineePassword(Credentials credentials, String newPassword) {
        authenticate(credentials);
        log.info("Facade: change trainee password, username={}", credentials.username());
        traineeService.changePassword(credentials.username(), newPassword);
    }

    public void toggleTraineeActive(Credentials credentials, String traineeUsername) {
        authenticate(credentials);
        log.info("Facade: toggle trainee active status, username={}", traineeUsername);
        traineeService.toggleActive(traineeUsername);
    }

    public void deleteTraineeProfile(Credentials credentials, String traineeUsername) {
        authenticate(credentials);
        log.info("Facade: delete trainee profile, username={}", traineeUsername);
        traineeService.deleteTraineeProfile(traineeUsername);
    }

    public Optional<Trainee> getTraineeProfile(Credentials credentials, String traineeUsername) {
        authenticate(credentials);
        log.debug("Facade: get trainee profile, username={}", traineeUsername);
        return traineeService.getTraineeProfile(traineeUsername);
    }

    public List<Trainee> getAllTrainees(Credentials credentials) {
        authenticate(credentials);
        log.debug("Facade: get all trainees");
        return traineeService.getAllTrainees();
    }

    public List<Trainer> getTrainersNotAssigned(Credentials credentials, String traineeUsername) {
        authenticate(credentials);
        log.debug("Facade: get trainers not assigned to trainee, username={}", traineeUsername);
        return traineeService.getTrainersNotAssigned(traineeUsername);
    }

    public List<Trainer> updateTraineeTrainers(Credentials credentials, String traineeUsername,
                                               List<String> trainerUsernames) {
        authenticate(credentials);
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

    public Trainer updateTrainerProfile(Credentials credentials, Trainer trainer) {
        authenticate(credentials);
        log.info("Facade: update trainer profile, username={}", trainer.getUsername());
        return trainerService.updateTrainerProfile(trainer);
    }

    public void changeTrainerPassword(Credentials credentials, String newPassword) {
        authenticate(credentials);
        log.info("Facade: change trainer password, username={}", credentials.username());
        trainerService.changePassword(credentials.username(), newPassword);
    }

    public void toggleTrainerActive(Credentials credentials, String trainerUsername) {
        authenticate(credentials);
        log.info("Facade: toggle trainer active status, username={}", trainerUsername);
        trainerService.toggleActive(trainerUsername);
    }

    public Optional<Trainer> getTrainerProfile(Credentials credentials, String trainerUsername) {
        authenticate(credentials);
        log.debug("Facade: get trainer profile, username={}", trainerUsername);
        return trainerService.getTrainerProfile(trainerUsername);
    }

    public List<Trainer> getAllTrainers(Credentials credentials) {
        authenticate(credentials);
        log.debug("Facade: get all trainers");
        return trainerService.getAllTrainers();
    }


    public Training createTraining(Credentials credentials, Training training) {
        authenticate(credentials);
        log.info("Facade: create training '{}'", training.getTrainingName());
        return trainingService.createTraining(training);
    }

    public Optional<Training> getTraining(Credentials credentials, Long id) {
        authenticate(credentials);
        log.debug("Facade: get training, id={}", id);
        return trainingService.getTraining(id);
    }

    public List<Training> getAllTrainings(Credentials credentials) {
        authenticate(credentials);
        log.debug("Facade: get all trainings");
        return trainingService.getAllTrainings();
    }

    public List<Training> getTraineeTrainings(Credentials credentials, String traineeUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        authenticate(credentials);
        log.debug("Facade: get trainee trainings, username={}", traineeUsername);
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, String trainerUsername,
                                              LocalDate fromDate, LocalDate toDate, String traineeName) {
        authenticate(credentials);
        log.debug("Facade: get trainer trainings, username={}", trainerUsername);
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }

    public void changePassword(Credentials credentials, String newPassword) {
        authenticate(credentials);

        log.info("Facade: change password, username={}", credentials.username());

        if (traineeService.existsByUsername(credentials.username())) {
            traineeService.changePassword(credentials.username(), newPassword);
        } else {
            trainerService.changePassword(credentials.username(), newPassword);
        }
    }
}
