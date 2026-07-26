package io.github.mrergos.gymcrm.dao;

import io.github.mrergos.gymcrm.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {

    Optional<TrainingType> findById(Long id);

    Optional<TrainingType> findByName(String trainingTypeName);

    List<TrainingType> findAll();
}