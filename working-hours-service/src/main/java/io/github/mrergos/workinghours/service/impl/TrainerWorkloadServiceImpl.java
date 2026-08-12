package io.github.mrergos.workinghours.service.impl;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.MonthSummaryResponse;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.dto.response.YearSummaryResponse;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.entity.MonthlySummary;
import io.github.mrergos.workinghours.entity.Trainer;
import io.github.mrergos.workinghours.exception.EntityNotFoundException;
import io.github.mrergos.workinghours.repository.MonthlySummaryRepository;
import io.github.mrergos.workinghours.repository.TrainerRepository;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private final TrainerRepository trainerRepository;
    private final MonthlySummaryRepository monthlySummaryRepository;

    public TrainerWorkloadServiceImpl(TrainerRepository trainerRepository,
                                       MonthlySummaryRepository monthlySummaryRepository) {
        this.trainerRepository = trainerRepository;
        this.monthlySummaryRepository = monthlySummaryRepository;
    }

    @Override
    @Transactional
    public void applyWorkload(TrainerWorkloadRequest request) {
        Assert.notNull(request, "Workload request must not be null");

        Trainer trainer = trainerRepository.findById(request.trainerUsername())
                .orElseGet(() -> {
                    log.info("First workload event for trainer, creating record, username={}", request.trainerUsername());
                    return new Trainer(request.trainerUsername(), request.trainerFirstName(),
                            request.trainerLastName(), Boolean.TRUE.equals(request.isActive()));
                });

        trainer.setFirstName(request.trainerFirstName());
        trainer.setLastName(request.trainerLastName());
        trainer.setActive(Boolean.TRUE.equals(request.isActive()));
        trainer = trainerRepository.save(trainer);

        LocalDate date = request.trainingDate();
        int year = date.getYear();
        int month = date.getMonthValue();

        Trainer finalTrainer = trainer;
        MonthlySummary summary = monthlySummaryRepository
                .findByTrainer_UsernameAndYearAndMonth(trainer.getUsername(), year, month)
                .orElseGet(() -> new MonthlySummary(finalTrainer, year, month, 0));

        applyDelta(summary, request.actionType(), request.trainingDuration(), trainer.getUsername(), year, month);

        monthlySummaryRepository.save(summary);
        log.info("Applied {} of {} minutes for trainer={}, year={}, month={}, newTotal={}",
                request.actionType(), request.trainingDuration(), trainer.getUsername(), year, month,
                summary.getTotalDurationMinutes());
    }

    private void applyDelta(MonthlySummary summary, ActionType actionType, int durationMinutes,
                             String username, int year, int month) {
        if (actionType == ActionType.ADD) {
            summary.setTotalDurationMinutes(summary.getTotalDurationMinutes() + durationMinutes);
            return;
        }

        int newTotal = summary.getTotalDurationMinutes() - durationMinutes;
        if (newTotal < 0) {
            log.warn("DELETE would drop total duration below zero for trainer={}, year={}, month={} " +
                            "(current={}, requestedDelta={}); clamping to 0",
                    username, year, month, summary.getTotalDurationMinutes(), durationMinutes);
            newTotal = 0;
        }
        summary.setTotalDurationMinutes(newTotal);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerWorkloadSummaryResponse getSummary(String username) {
        Trainer trainer = trainerRepository.findWithSummariesByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Workload summary requested for unknown trainer, username={}", username);
                    return new EntityNotFoundException("Trainer not found: " + username);
                });

        List<YearSummaryResponse> years = groupByYear(trainer);

        log.debug("Built workload summary for trainer={}, years={}", username, years.size());
        return new TrainerWorkloadSummaryResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                years
        );
    }

    private List<YearSummaryResponse> groupByYear(Trainer trainer) {
        Map<Integer, List<MonthlySummary>> byYear = trainer.getSummaries().stream()
                .collect(Collectors.groupingBy(MonthlySummary::getYear, TreeMap::new, Collectors.toList()));

        return byYear.entrySet().stream()
                .map(entry -> new YearSummaryResponse(entry.getKey(), toMonthResponses(entry.getValue())))
                .toList();
    }

    private List<MonthSummaryResponse> toMonthResponses(List<MonthlySummary> summaries) {
        return summaries.stream()
                .sorted(Comparator.comparingInt(MonthlySummary::getMonth))
                .map(s -> new MonthSummaryResponse(s.getMonth(), s.getTotalDurationMinutes()))
                .toList();
    }
}
