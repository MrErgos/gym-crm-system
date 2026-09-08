package io.github.mrergos.workinghours.service.impl;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.MonthSummaryResponse;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.dto.response.YearSummaryResponse;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.entity.MonthlySummary;
import io.github.mrergos.workinghours.entity.TrainerWorkloadSummaryDocument;
import io.github.mrergos.workinghours.entity.YearSummary;
import io.github.mrergos.workinghours.exception.EntityNotFoundException;
import io.github.mrergos.workinghours.repository.TrainerWorkloadSummaryRepository;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private final TrainerWorkloadSummaryRepository trainerSummaryRepository;

    public TrainerWorkloadServiceImpl(TrainerWorkloadSummaryRepository trainerSummaryRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
    }

    @Override
    @Transactional
    public void applyWorkload(TrainerWorkloadRequest request) {
        Assert.notNull(request, "Workload request must not be null");
        Assert.hasText(request.trainerUsername(), "Trainer username must not be blank");
        Assert.notNull(request.trainingDate(), "Training date must not be null");
        Assert.notNull(request.trainingDuration(), "Training duration must not be null");
        Assert.notNull(request.actionType(), "Action type must not be null");
        
        log.info("Applying workload event: trainer={}, action={}, date={}",
                request.trainerUsername(), request.actionType(), request.trainingDate());

        TrainerWorkloadSummaryDocument summaryDocument = trainerSummaryRepository.findById(request.trainerUsername())
                .orElseGet(() -> createNewDocument(request));

        summaryDocument.setTrainerFirstName(request.trainerFirstName());
        summaryDocument.setTrainerLastName(request.trainerLastName());
        summaryDocument.setTrainerStatus(Boolean.TRUE.equals(request.isActive()));

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();
        MonthlySummary monthlySummary = findOrCreateMonthlySummary(summaryDocument, request.trainingDate());

        applyDelta(monthlySummary, request.actionType(), request.trainingDuration(),
                request.trainerUsername(), year, month);

        trainerSummaryRepository.save(summaryDocument);

        log.info("Workload event applied: trainer={}, year={}, month={}, newTotal={}min",
                request.trainerUsername(), year, month,
                monthlySummary.getTotalDurationMinutes());
    }

    private TrainerWorkloadSummaryDocument createNewDocument(TrainerWorkloadRequest request) {
        log.debug("No existing summary document for trainer={}, creating new one", request.trainerUsername());
        return new TrainerWorkloadSummaryDocument(
                request.trainerUsername(),
                request.trainerFirstName(),
                request.trainerLastName(),
                Boolean.TRUE.equals(request.isActive())
        );
    }

    private MonthlySummary findOrCreateMonthlySummary(TrainerWorkloadSummaryDocument document, LocalDate trainingDate) {
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();

        YearSummary yearSummary = document.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary created = new YearSummary(year);
                    document.getYears().add(created);
                    log.debug("Created new year summary: year={}", year);
                    return created;
                });

        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthlySummary created = new MonthlySummary(month, 0);
                    yearSummary.getMonths().add(created);
                    log.debug("Created new month summary: year={}, month={}", year, month);
                    return created;
                });
    }

    private void applyDelta(MonthlySummary summary, ActionType actionType, int durationMinutes,
                             String username, int year, int month) {
        if (actionType == ActionType.ADD) {
            summary.setTotalDurationMinutes(summary.getTotalDurationMinutes() + durationMinutes);
            log.debug("ADD applied: trainer={}, year={}, month={}, delta={}min",
                    username, year, month, durationMinutes);
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
    public TrainerWorkloadSummaryResponse getSummary(String username) {
        Assert.hasText(username, "Trainer username must not be blank");

        log.info("Fetching workload summary for trainer={}", username);

        TrainerWorkloadSummaryDocument document = trainerSummaryRepository.findById(username)
                .orElseThrow(() -> {
                    log.warn("Workload summary requested for unknown trainer, username={}", username);
                    return new EntityNotFoundException("Trainer not found: " + username);
                });

        List<YearSummaryResponse> years = document.getYears().stream()
                .sorted(Comparator.comparingInt(YearSummary::getYear))
                .map(this::toYearSummaryResponse)
                .toList();

        log.debug("Built workload summary for trainer={}, years={}", username, years.size());
        return new TrainerWorkloadSummaryResponse(
                document.getTrainerUsername(),
                document.getTrainerFirstName(),
                document.getTrainerLastName(),
                document.isTrainerStatus(),
                years
        );
    }

    private YearSummaryResponse toYearSummaryResponse(YearSummary yearSummary) {
        List<MonthSummaryResponse> months = yearSummary.getMonths().stream()
                .sorted(Comparator.comparingInt(MonthlySummary::getMonth))
                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getTotalDurationMinutes()))
                .toList();
        return new YearSummaryResponse(yearSummary.getYear(), months);
    }
}
