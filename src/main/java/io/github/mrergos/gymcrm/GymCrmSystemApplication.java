package io.github.mrergos.gymcrm;


import io.github.mrergos.gymcrm.config.Config;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
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
        log.info("Staring Gym CRM System application...");

        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            demoTrainee(facade);
            demoTrainer(facade);
            demoTraining(facade);

            log.info("Gym CRM application finished successfully.");
        }
    }

    private static void demoTrainee(GymFacade facade) {
        log.info("=== Trainee operations ===");


        Trainee trainee = facade.createTraineeProfile(
                "John", "Doe", LocalDate.of(1990, 5, 15), "123 Main St");
        log.info("Created: id={}, username={}", trainee.getUserId(), trainee.getUsername());

        Trainee duplicate = facade.createTraineeProfile(
                "John", "Doe", LocalDate.of(1992, 3, 20), "456 Oak Ave");
        log.info("Duplicate: id={}, username={}", duplicate.getUserId(), duplicate.getUsername());


        facade.getTraineeProfile(trainee.getUserId())
                .ifPresent(t -> log.info("Found trainee: {}", t.getUsername()));


        trainee.setAddress("789 Pine Rd");
        facade.updateTraineeProfile(trainee);
        log.info("Updated trainee address");


        List<Trainee> all = facade.getAllTrainees();
        log.info("Total trainees in storage: {}", all.size());


        facade.deleteTraineeProfile(trainee.getUserId());
        log.info("Deleted trainee, id={}", trainee.getUserId());
    }

    private static void demoTrainer(GymFacade facade) {
        log.info("=== Trainer operations ===");

        TrainingType yoga = new TrainingType("yoga");

        Trainer trainer = facade.createTrainerProfile("Jane", "Smith", yoga);
        log.info("Created: id={}, username={}", trainer.getUserId(), trainer.getUsername());

        facade.getTrainerProfile(trainer.getUserId())
                .ifPresent(t -> log.info("Found trainer: {} ({})",
                        t.getUsername(), t.getSpecialization().getTrainingTypeName()));

        trainer.setSpecialization(new TrainingType("fitness"));
        facade.updateTrainerProfile(trainer);
        log.info("Updated trainer specialization");

        List<Trainer> all = facade.getAllTrainers();
        log.info("Total trainers in storage: {}", all.size());
    }

    private static void demoTraining(GymFacade facade) {
        log.info("=== Training operations ===");

        Trainee trainee = facade.getAllTrainees().stream().findFirst().orElseThrow();
        Trainer trainer = facade.getAllTrainers().stream().findFirst().orElseThrow();

        Training training = new Training();
        training.setTraineeId(trainee.getUserId());
        training.setTrainerId(trainer.getUserId());
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(new TrainingType("yoga"));
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        Training saved = facade.createTraining(training);
        log.info("Created training: id={}, name='{}'", saved.getId(), saved.getTrainingName());

        facade.getTraining(saved.getId())
                .ifPresent(t -> log.info("Found training: '{}', duration={} min",
                        t.getTrainingName(), t.getTrainingDuration()));

        List<Training> all = facade.getAllTrainings();
        log.info("Total trainings in storage: {}", all.size());
    }


}
