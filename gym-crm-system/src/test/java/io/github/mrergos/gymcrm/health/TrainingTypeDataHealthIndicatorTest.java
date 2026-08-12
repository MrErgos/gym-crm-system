package io.github.mrergos.gymcrm.health;

import io.github.mrergos.gymcrm.dao.TrainingTypeDao;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingTypeDataHealthIndicator tests")
class TrainingTypeDataHealthIndicatorTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TrainingTypeDataHealthIndicator trainingTypeSeedHealthIndicator;

    private TrainingType buildTrainingType(Long id, String name) {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(id);
        trainingType.setTrainingTypeName(name);
        return trainingType;
    }

    @Test
    @DisplayName("health: return up with count when training types exist")
    void health_whenTrainingTypesExist_shouldReturnUpWithCount() {
        // given
        List<TrainingType> trainingTypes = List.of(
                buildTrainingType(1L, "Cardio"),
                buildTrainingType(2L, "Strength")
        );
        when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<List<TrainingType>> callback = invocation.getArgument(0);
                    return callback.doInTransaction(mock(TransactionStatus.class));
                });
        when(trainingTypeDao.findAll()).thenReturn(trainingTypes);

        // when
        Health result = trainingTypeSeedHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("trainingTypesCount", 2);
    }

    @Test
    @DisplayName("health: return down when training types are empty")
    void health_whenTrainingTypesEmpty_shouldReturnDown() {
        // given
        when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<List<TrainingType>> callback = invocation.getArgument(0);
                    return callback.doInTransaction(mock(TransactionStatus.class));
                });
        when(trainingTypeDao.findAll()).thenReturn(Collections.emptyList());

        // when
        Health result = trainingTypeSeedHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsEntry("trainingTypesCount", 0);
        assertThat(result.getDetails()).containsKey("reason");
    }

    @Test
    @DisplayName("health: return down when transaction throws exception")
    void health_whenTransactionExecutionThrowsException_shouldReturnDown() {
        // given
        when(transactionTemplate.execute(any()))
                .thenThrow(new RuntimeException("DB unavailable"));

        // when
        Health result = trainingTypeSeedHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsKey("error");
    }

    @Test
    @DisplayName("health: return down with zero count when transaction template returns null")
    void health_whenTransactionTemplateReturnsNull_shouldReturnDownWithZeroCount() {
        // given
        when(transactionTemplate.execute(any()))
                .thenReturn(null);

        // when
        Health result = trainingTypeSeedHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsEntry("trainingTypesCount", 0);
    }
}