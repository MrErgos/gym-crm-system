package io.github.mrergos.gymcrm.dao;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);

    Optional<Trainee> findById(Long id);

    Optional<Trainee> findByUsername(String username);

    List<Trainee> findAll();

    void delete(Trainee trainee);

    boolean existsByUsername(String username);

    List<Trainer> updateTrainers(String traineeUsername, List<String> trainerUsernames);
}
