package io.github.mrergos.gymcrm.dao;

import io.github.mrergos.gymcrm.entity.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training save(Training training);

    Optional<Training> findById(Long id);

    List<Training> findAll();
}
