package io.github.mrergos.gymcrm;


import io.github.mrergos.gymcrm.config.Config;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.facade.Credentials;
import io.github.mrergos.gymcrm.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymCrmSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(GymCrmSystemApplication.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM System application...");

        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainee trainee = demoTrainee(facade);
            Trainer trainer = demoTrainer(facade);
            demoTraining(facade, trainee, trainer);

            log.info("Gym CRM application finished successfully.");
        }
    }


    private static Trainee demoTrainee(GymFacade facade) {
        log.info("=== Trainee operations ===");


        Trainee trainee = facade.createTraineeProfile(
                "John", "Doe", LocalDate.of(1990, 5, 15), "123 Main St");
        log.info("Created: id={}, username={}, password={}",
                trainee.getId(), trainee.getUsername(), trainee.getPassword());

        Credentials traineeCreds = new Credentials(trainee.getUsername(), trainee.getPassword());

        Trainee duplicate = facade.createTraineeProfile(
                "John", "Doe", LocalDate.of(1992, 3, 20), "456 Oak Ave");
        log.info("Duplicate: id={}, username={}", duplicate.getId(), duplicate.getUsername());

        facade.getTraineeProfile(traineeCreds, trainee.getUsername())
                .ifPresent(t -> log.info("Found trainee: {}", t.getUsername()));


        trainee.setAddress("789 Pine Rd");
        Trainee updated = facade.updateTraineeProfile(traineeCreds, trainee);
        log.info("Updated trainee address to: {}", updated.getAddress());

        String newPassword = "newSecurePass123";
        facade.changeTraineePassword(traineeCreds, newPassword);
        log.info("Trainee password changed");
        traineeCreds = new Credentials(trainee.getUsername(), newPassword);


        facade.toggleTraineeActive(traineeCreds, trainee.getUsername());
        log.info("Trainee active status toggled");

        List<Trainee> all = facade.getAllTrainees(traineeCreds);
        log.info("Total trainees in storage: {}", all.size());

        Credentials duplicateCreds = new Credentials(duplicate.getUsername(), duplicate.getPassword());
        facade.deleteTraineeProfile(duplicateCreds, duplicate.getUsername());
        log.info("Deleted duplicate trainee, username={}", duplicate.getUsername());

        return trainee;
    }

    private static Trainer demoTrainer(GymFacade facade) {
        log.info("=== Trainer operations ===");

        TrainingType yoga = facade.getAvailableTrainingTypes().stream()
                .filter(trainingType -> "Yoga".equals(trainingType.getTrainingTypeName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Training type 'Yoga' not found - check data.sql seeding"));


        Trainer trainer = facade.createTrainerProfile("Jane", "Smith", yoga);
        log.info("Created: id={}, username={}, password={}",
                trainer.getId(), trainer.getUsername(), trainer.getPassword());

        Credentials trainerCreds = new Credentials(trainer.getUsername(), trainer.getPassword());

        facade.getTrainerProfile(trainerCreds, trainer.getUsername())
                .ifPresent(t -> log.info("Found trainer: {} ({})",
                        t.getUsername(), t.getSpecialization().getTrainingTypeName()));

        trainer.setLastName("Smith-Connor");
        Trainer updatedTrainer = facade.updateTrainerProfile(trainerCreds, trainer);
        log.info("Updated trainer last name to: {}", updatedTrainer.getLastName());


        String newPassword = "trainerPass456";
        facade.changeTrainerPassword(trainerCreds, newPassword);
        log.info("Trainer password changed");
        trainerCreds = new Credentials(trainer.getUsername(), newPassword);

        List<Trainer> all = facade.getAllTrainers(trainerCreds);
        log.info("Total trainers in storage: {}", all.size());

        return trainer;
    }

    private static void demoTraining(GymFacade facade, Trainee trainee, Trainer trainer) {
        log.info("=== Training operations ===");


        Credentials traineeCreds = new Credentials(trainee.getUsername(), "newSecurePass123");
        Credentials trainerCreds = new Credentials(trainer.getUsername(), "trainerPass456");

        List<Trainer> unassigned = facade.getTrainersNotAssigned(traineeCreds, trainee.getUsername());
        log.info("Trainers not yet assigned to trainee {}: {}", trainee.getUsername(), unassigned.size());

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        Training saved = facade.createTraining(traineeCreds, training);
        log.info("Created training: id={}, name='{}'", saved.getId(), saved.getTrainingName());

        facade.getTraining(traineeCreds, saved.getId())
                .ifPresent(t -> log.info("Found training: '{}', duration={} min",
                        t.getTrainingName(), t.getTrainingDuration()));

        List<Training> traineeTrainings = facade.getTraineeTrainings(
                traineeCreds, trainee.getUsername(), null, null, null, null);
        log.info("Trainee {} has {} training(s)", trainee.getUsername(), traineeTrainings.size());


        List<Training> trainerTrainings = facade.getTrainerTrainings(
                trainerCreds, trainer.getUsername(), null, null, null);
        log.info("Trainer {} has {} training(s)", trainer.getUsername(), trainerTrainings.size());


        List<Trainer> updatedTrainers = facade.updateTraineeTrainers(
                traineeCreds, trainee.getUsername(), List.of(trainer.getUsername()));
        log.info("Trainee {} now has {} assigned trainer(s)", trainee.getUsername(), updatedTrainers.size());

        List<Training> all = facade.getAllTrainings(traineeCreds);
        log.info("Total trainings in storage: {}", all.size());
    }
}
