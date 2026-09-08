package io.github.mrergos.gymcrm.health;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActiveUsersHealthIndicator tests")
class ActiveUsersHealthIndicatorTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ActiveUsersHealthIndicator activeUsersHealthIndicator;

    private Trainee buildTrainee(boolean active) {
        Trainee trainee = new Trainee();
        trainee.setActive(active);
        return trainee;
    }

    private Trainer buildTrainer(boolean active) {
        Trainer trainer = new Trainer();
        trainer.setActive(active);
        return trainer;
    }

    private void stubTransactionTemplateExecution() {
        when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<long[]> callback = invocation.getArgument(0);
                    return callback.doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class));
                });
    }

    @Test
    @DisplayName("health: return up with count when active users exist")
    void health_whenActiveUsersExist_shouldReturnUpWithCounts() {
        // given
        stubTransactionTemplateExecution();
        List<Trainee> trainees = List.of(buildTrainee(true), buildTrainee(true), buildTrainee(false));
        List<Trainer> trainers = List.of(buildTrainer(true), buildTrainer(false));
        when(traineeDao.findAll()).thenReturn(trainees);
        when(trainerDao.findAll()).thenReturn(trainers);

        // when
        Health result = activeUsersHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("activeTrainees", 2L);
        assertThat(result.getDetails()).containsEntry("activeTrainers", 1L);
    }

    @Test
    @DisplayName("health: return up wit zero count when no users exist")
    void health_whenNoUsersExist_shouldReturnUpWithZeroCounts() {
        // given
        stubTransactionTemplateExecution();
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());

        // when
        Health result = activeUsersHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("activeTrainees", 0L);
        assertThat(result.getDetails()).containsEntry("activeTrainers", 0L);
    }

    @Test
    @DisplayName("health: return up with zero count when no active users")
    void health_whenNoActiveUsers_shouldReturnUpWithZeroCounts() {
        // given
        stubTransactionTemplateExecution();
        when(traineeDao.findAll()).thenReturn(List.of(buildTrainee(false), buildTrainee(false)));
        when(trainerDao.findAll()).thenReturn(List.of(buildTrainer(false)));

        // when
        Health result = activeUsersHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("activeTrainees", 0L);
        assertThat(result.getDetails()).containsEntry("activeTrainers", 0L);
    }

    @Test
    @DisplayName("health: return down when dao throws exception")
    void health_whenDaoThrowsException_shouldReturnDown() {
        // given
        when(transactionTemplate.execute(any()))
                .thenThrow(new RuntimeException("DB unavailable"));

        // when
        Health result = activeUsersHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsKey("error");
    }
}