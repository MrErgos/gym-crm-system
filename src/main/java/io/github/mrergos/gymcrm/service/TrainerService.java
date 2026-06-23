package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization);

    Trainer updateTrainerProfile(Trainer trainer);

    Optional<Trainer> getTrainerProfile(Long id);

    List<Trainer> getAllTrainers();
}
