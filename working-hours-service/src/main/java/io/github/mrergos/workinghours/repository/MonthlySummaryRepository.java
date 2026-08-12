package io.github.mrergos.workinghours.repository;

import io.github.mrergos.workinghours.entity.MonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {

    Optional<MonthlySummary> findByTrainer_UsernameAndYearAndMonth(String username, int year, int month);
}
