package io.github.mrergos.workinghours.service.impl;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.dto.response.YearSummaryResponse;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.entity.MonthlySummary;
import io.github.mrergos.workinghours.entity.Trainer;
import io.github.mrergos.workinghours.exception.EntityNotFoundException;
import io.github.mrergos.workinghours.repository.MonthlySummaryRepository;
import io.github.mrergos.workinghours.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadServiceImpl")
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private MonthlySummaryRepository monthlySummaryRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    @Captor
    private ArgumentCaptor<Trainer> trainerCaptor;

    @Captor
    private ArgumentCaptor<MonthlySummary> summaryCaptor;

    private static final String USERNAME = "John.Doe";
    private static final int YEAR = 2026;
    private static final int MONTH = 8;

    private TrainerWorkloadRequest buildRequest(ActionType actionType, int duration, boolean isActive) {
        return new TrainerWorkloadRequest(
                USERNAME,
                "John",
                "Doe",
                isActive,
                LocalDate.of(YEAR, MONTH, 1),
                duration,
                actionType
        );
    }

    @Nested
    @DisplayName("applyWorkload")
    class ApplyWorkload {

        @Test
        @DisplayName("creates a new trainer when none exists yet")
        void createsNewTrainerWhenNotFound() {
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.empty());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.empty());

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 60, true);

            service.applyWorkload(request);

            verify(trainerRepository).save(trainerCaptor.capture());
            Trainer savedTrainer = trainerCaptor.getValue();
            assertThat(savedTrainer.getUsername()).isEqualTo(USERNAME);
            assertThat(savedTrainer.getFirstName()).isEqualTo("John");
            assertThat(savedTrainer.getLastName()).isEqualTo("Doe");
            assertThat(savedTrainer.isActive()).isTrue();
        }

        @Test
        @DisplayName("updates existing trainer's name and active status")
        void updatesExistingTrainer() {
            Trainer existing = new Trainer(USERNAME, "Old", "Name", false);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(existing));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.empty());

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 30, true);

            service.applyWorkload(request);

            verify(trainerRepository).save(trainerCaptor.capture());
            Trainer saved = trainerCaptor.getValue();
            assertThat(saved.getFirstName()).isEqualTo("John");
            assertThat(saved.getLastName()).isEqualTo("Doe");
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("ADD action increases total duration for a new monthly summary")
        void addActionOnNewSummary() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.empty());

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 60, true);

            service.applyWorkload(request);

            verify(monthlySummaryRepository).save(summaryCaptor.capture());
            assertThat(summaryCaptor.getValue().getTotalDurationMinutes()).isEqualTo(60);
        }

        @Test
        @DisplayName("ADD action accumulates duration onto existing summary")
        void addActionAccumulatesOnExistingSummary() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            MonthlySummary existingSummary = new MonthlySummary(trainer, YEAR, MONTH, 90);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.of(existingSummary));

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 30, true);

            service.applyWorkload(request);

            verify(monthlySummaryRepository).save(summaryCaptor.capture());
            assertThat(summaryCaptor.getValue().getTotalDurationMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("DELETE action decreases total duration")
        void deleteActionDecreasesTotal() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            MonthlySummary existingSummary = new MonthlySummary(trainer, YEAR, MONTH, 90);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.of(existingSummary));

            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 30, true);

            service.applyWorkload(request);

            verify(monthlySummaryRepository).save(summaryCaptor.capture());
            assertThat(summaryCaptor.getValue().getTotalDurationMinutes()).isEqualTo(60);
        }

        @Test
        @DisplayName("DELETE action clamps total duration to zero instead of going negative")
        void deleteActionClampsToZero() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            MonthlySummary existingSummary = new MonthlySummary(trainer, YEAR, MONTH, 20);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.of(existingSummary));

            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 50, true);

            service.applyWorkload(request);

            verify(monthlySummaryRepository).save(summaryCaptor.capture());
            assertThat(summaryCaptor.getValue().getTotalDurationMinutes()).isEqualTo(0);
        }

        @Test
        @DisplayName("DELETE action resulting in exactly zero does not clamp/log a warning path issue")
        void deleteActionExactlyZero() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            MonthlySummary existingSummary = new MonthlySummary(trainer, YEAR, MONTH, 60);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                    .thenReturn(Optional.of(existingSummary));

            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 60, true);

            service.applyWorkload(request);

            verify(monthlySummaryRepository).save(summaryCaptor.capture());
            assertThat(summaryCaptor.getValue().getTotalDurationMinutes()).isEqualTo(0);
        }

        @Test
        @DisplayName("throws IllegalArgumentException (via Assert.notNull) when request is null")
        void throwsWhenRequestIsNull() {
            assertThatThrownBy(() -> service.applyWorkload(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(trainerRepository, never()).findById(any());
            verify(monthlySummaryRepository, never()).save(any());
        }

        @Test
        @DisplayName("resolves year and month from the training date correctly")
        void resolvesYearAndMonthFromDate() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                    USERNAME, "John", "Doe", true, LocalDate.of(2025, 12, 25), 45, ActionType.ADD);

            when(monthlySummaryRepository.findByTrainer_UsernameAndYearAndMonth(USERNAME, 2025, 12))
                    .thenReturn(Optional.empty());

            service.applyWorkload(request);

            verify(monthlySummaryRepository).findByTrainer_UsernameAndYearAndMonth(USERNAME, 2025, 12);
        }
    }

    @Nested
    @DisplayName("getSummary")
    class GetSummary {

        @Test
        @DisplayName("throws EntityNotFoundException when trainer does not exist")
        void throwsWhenTrainerNotFound() {
            when(trainerRepository.findWithSummariesByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSummary(USERNAME))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(USERNAME);
        }

        @Test
        @DisplayName("returns summary with correct trainer fields")
        void returnsSummaryWithTrainerFields() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            when(trainerRepository.findWithSummariesByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            TrainerWorkloadSummaryResponse response = service.getSummary(USERNAME);

            assertThat(response.trainerUsername()).isEqualTo(USERNAME);
            assertThat(response.trainerFirstName()).isEqualTo("John");
            assertThat(response.trainerLastName()).isEqualTo("Doe");
            assertThat(response.trainerStatus()).isTrue();
            assertThat(response.years()).isEmpty();
        }

        @Test
        @DisplayName("groups monthly summaries by year, sorted by year and month")
        void groupsSummariesByYearAndMonth() {
            Trainer trainer = new Trainer(USERNAME, "John", "Doe", true);
            trainer.getSummaries().add(new MonthlySummary(trainer, 2026, 3, 120));
            trainer.getSummaries().add(new MonthlySummary(trainer, 2025, 12, 60));
            trainer.getSummaries().add(new MonthlySummary(trainer, 2026, 1, 90));

            when(trainerRepository.findWithSummariesByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            TrainerWorkloadSummaryResponse response = service.getSummary(USERNAME);

            assertThat(response.years()).hasSize(2);

            YearSummaryResponse year2025 = response.years().get(0);
            assertThat(year2025.year()).isEqualTo(2025);
            assertThat(year2025.months()).hasSize(1);
            assertThat(year2025.months().get(0).month()).isEqualTo(12);
            assertThat(year2025.months().get(0).trainingSummaryDuration()).isEqualTo(60);

            YearSummaryResponse year2026 = response.years().get(1);
            assertThat(year2026.year()).isEqualTo(2026);
            assertThat(year2026.months()).extracting("month").containsExactly(1, 3);
        }
    }
}
