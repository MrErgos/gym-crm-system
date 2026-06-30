package io.github.mrergos.gymcrm.dao;

import io.github.mrergos.gymcrm.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);

    Optional<Trainee> findById(Long id);

    List<Trainee> findAll();

    void delete(Long id);

    boolean existsByUsername(String username);
}
