package io.github.mrergos.workinghours.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.dto.response.YearSummaryResponse;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.entity.MonthlySummary;
import io.github.mrergos.workinghours.entity.TrainerWorkloadSummaryDocument;
import io.github.mrergos.workinghours.entity.YearSummary;
import io.github.mrergos.workinghours.exception.EntityNotFoundException;
import io.github.mrergos.workinghours.repository.TrainerWorkloadSummaryRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadServiceImpl")
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadSummaryRepository trainerSummaryRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    @Captor
    private ArgumentCaptor<TrainerWorkloadSummaryDocument> summaryDocumentCaptor;

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
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.empty());
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 60, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            TrainerWorkloadSummaryDocument saved = summaryDocumentCaptor.getValue();
            assertThat(saved.getTrainerUsername()).isEqualTo(USERNAME);
            assertThat(saved.getTrainerFirstName()).isEqualTo("John");
            assertThat(saved.getTrainerLastName()).isEqualTo("Doe");
            assertThat(saved.isTrainerStatus()).isTrue();
        }

        @Test
        @DisplayName("updates existing trainer's name and active status")
        void updatesExistingTrainer() {
            TrainerWorkloadSummaryDocument existing = new TrainerWorkloadSummaryDocument(USERNAME, "Old", "Name", false);
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(existing));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 30, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            TrainerWorkloadSummaryDocument saved = summaryDocumentCaptor.getValue();
            assertThat(saved.getTrainerFirstName()).isEqualTo("John");
            assertThat(saved.getTrainerLastName()).isEqualTo("Doe");
            assertThat(saved.isTrainerStatus()).isTrue();
        }

        @Test
        @DisplayName("ADD action increases total duration for a new monthly summary")
        void addActionOnNewSummary() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 60, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            int duration = monthDuration(summaryDocumentCaptor.getValue(), YEAR, MONTH);
            assertThat(duration).isEqualTo(60);
        }

        @Test
        @DisplayName("ADD action accumulates duration onto existing summary")
        void addActionAccumulatesOnExistingSummary() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            document.getYears().add(yearWithMonth(YEAR, MONTH, 90));
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 30, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            int duration = monthDuration(summaryDocumentCaptor.getValue(), YEAR, MONTH);
            assertThat(duration).isEqualTo(120);
        }

        @Test
        @DisplayName("DELETE action decreases total duration")
        void deleteActionDecreasesTotal() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            document.getYears().add(yearWithMonth(YEAR, MONTH, 90));
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 30, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            int duration = monthDuration(summaryDocumentCaptor.getValue(), YEAR, MONTH);
            assertThat(duration).isEqualTo(60);
        }

        @Test
        @DisplayName("DELETE action clamps total duration to zero instead of going negative")
        void deleteActionClampsToZero() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            document.getYears().add(yearWithMonth(YEAR, MONTH, 20));
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 50, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            int duration = monthDuration(summaryDocumentCaptor.getValue(), YEAR, MONTH);
            assertThat(duration).isEqualTo(0);
        }

        @Test
        @DisplayName("DELETE action resulting in exactly zero does not clamp/log a warning path issue")
        void deleteActionExactlyZero() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            document.getYears().add(yearWithMonth(YEAR, MONTH, 60));
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 60, true);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            int duration = monthDuration(summaryDocumentCaptor.getValue(), YEAR, MONTH);
            assertThat(duration).isEqualTo(0);
        }

        @Test
        @DisplayName("throws IllegalArgumentException (via Assert.notNull) when request is null")
        void throwsWhenRequestIsNull() {
            assertThatThrownBy(() -> service.applyWorkload(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(trainerSummaryRepository, never()).findById(any());
            verify(trainerSummaryRepository, never()).save(any());
        }

        @Test
        @DisplayName("resolves year and month from the training date correctly")
        void resolvesYearAndMonthFromDate() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));
            when(trainerSummaryRepository.save(any(TrainerWorkloadSummaryDocument.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                    USERNAME, "John", "Doe", true, LocalDate.of(2025, 12, 25), 45, ActionType.ADD);

            service.applyWorkload(request);

            verify(trainerSummaryRepository).save(summaryDocumentCaptor.capture());
            int duration = monthDuration(summaryDocumentCaptor.getValue(), 2025, 12);
            assertThat(duration).isEqualTo(45);
        }
    }

    @Nested
    @DisplayName("getSummary")
    class GetSummary {

        @Test
        @DisplayName("throws EntityNotFoundException when trainer does not exist")
        void throwsWhenTrainerNotFound() {
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSummary(USERNAME))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(USERNAME);
        }

        @Test
        @DisplayName("returns summary with correct trainer fields")
        void returnsSummaryWithTrainerFields() {
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));

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
            TrainerWorkloadSummaryDocument document = new TrainerWorkloadSummaryDocument(USERNAME, "John", "Doe", true);
            YearSummary year2026 = new YearSummary(2026);
            year2026.getMonths().add(new MonthlySummary(3, 120));
            year2026.getMonths().add(new MonthlySummary(1, 90));
            YearSummary year2025 = new YearSummary(2025);
            year2025.getMonths().add(new MonthlySummary(12, 60));
            document.getYears().add(year2026);
            document.getYears().add(year2025);

            when(trainerSummaryRepository.findById(USERNAME)).thenReturn(Optional.of(document));

            TrainerWorkloadSummaryResponse response = service.getSummary(USERNAME);

            assertThat(response.years()).hasSize(2);

            YearSummaryResponse resultYear2025 = response.years().get(0);
            assertThat(resultYear2025.year()).isEqualTo(2025);
            assertThat(resultYear2025.months()).hasSize(1);
            assertThat(resultYear2025.months().get(0).month()).isEqualTo(12);
            assertThat(resultYear2025.months().get(0).trainingSummaryDuration()).isEqualTo(60);

            YearSummaryResponse resultYear2026 = response.years().get(1);
            assertThat(resultYear2026.year()).isEqualTo(2026);
            assertThat(resultYear2026.months()).extracting("month").containsExactly(1, 3);
        }
    }

    private YearSummary yearWithMonth(int year, int month, int duration) {
        YearSummary yearSummary = new YearSummary(year);
        yearSummary.getMonths().add(new MonthlySummary(month, duration));
        return yearSummary;
    }

    private int monthDuration(TrainerWorkloadSummaryDocument document, int year, int month) {
        return document.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .flatMap(y -> y.getMonths().stream().filter(m -> m.getMonth() == month).findFirst())
                .map(MonthlySummary::getTotalDurationMinutes)
                .orElseThrow(() -> new AssertionError("Month element not found: year=" + year + ", month=" + month));
    }
}