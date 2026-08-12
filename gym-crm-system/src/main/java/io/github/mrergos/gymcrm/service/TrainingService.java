package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.entity.Training;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training createTraining(Training training);

    Optional<Training> getTraining(Long id);

    List<Training> getAllTrainings();

    List<Training> getTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                       String trainerName, String trainingTypeName);

    List<Training> getTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                       String traineeName);
}
