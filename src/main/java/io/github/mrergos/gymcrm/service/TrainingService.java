package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.entity.Training;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training createTraining(Training training);

    Optional<Training> getTraining(Long id);

    List<Training> getAllTrainings();
}
