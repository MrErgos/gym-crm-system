package io.github.mrergos.workinghours.repository;

import io.github.mrergos.workinghours.entity.Trainer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, String> {

    @EntityGraph(attributePaths = "summaries")
    Optional<Trainer> findWithSummariesByUsername(String username);
}
