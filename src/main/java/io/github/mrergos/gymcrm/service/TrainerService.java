package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization);

    Trainer createTrainerProfile(String firstName, String lastName, Long specializationId);

    Trainer updateTrainerProfile(Trainer trainer);

    void changePassword(String username, String newPassword);

    void toggleActive(String username);

    Optional<Trainer> getTrainerProfile(String username);

    List<Trainer> getAllTrainers();

    List<TrainingType> getAvailableTrainingTypes();

    Optional<TrainingType> getTrainingTypeById(Long id);

    boolean existsByUsername(String username);
}
