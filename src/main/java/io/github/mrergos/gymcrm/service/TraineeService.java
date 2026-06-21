package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.entity.Trainee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address);

    Trainee updateTraineeProfile(Trainee trainee);

    void deleteTraineeProfile(Long id);

    Optional<Trainee> getTraineeProfile(Long id);

    List<Trainee> getAllTrainee();
}
