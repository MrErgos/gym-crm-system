package io.github.mrergos.gymcrm.dao;

import io.github.mrergos.gymcrm.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);

    Optional<Trainer> findById(Long id);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findAll();

    List<Trainer> findAllNotAssignedToTrainee(String traineeUsername);

    boolean existsByUsername(String username);
}
