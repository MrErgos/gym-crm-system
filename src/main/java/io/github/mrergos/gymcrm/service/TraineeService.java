package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address);

    Trainee updateTraineeProfile(Trainee trainee);

    void changePassword(String username, String newPassword);

    void toggleActive(String username);

    void deleteTraineeProfile(String username);

    Optional<Trainee> getTraineeProfile(String username);

    List<Trainee> getAllTrainees();

    List<Trainer> getTrainersNotAssigned(String traineeUsername);

    List<Trainer> updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames);
}
