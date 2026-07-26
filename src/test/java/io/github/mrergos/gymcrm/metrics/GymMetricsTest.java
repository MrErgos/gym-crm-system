package io.github.mrergos.gymcrm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymMetrics tests")
class GymMetricsTest {

    private MeterRegistry meterRegistry;
    private GymMetrics gymMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymMetrics = new GymMetrics(meterRegistry);
    }

    @Test
    @DisplayName("incrementTraineeRegistrations: increase counter by one when called")
    void incrementTraineeRegistrations_whenCalled_shouldIncreaseCounterByOne() {
        // given
        Counter counter = meterRegistry.get("gymcrm.trainee.registrations").counter();

        // when
        gymMetrics.incrementTraineeRegistrations();
        gymMetrics.incrementTraineeRegistrations();

        // then
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("incrementTrainerRegistrations: increase counter by one when called")
    void incrementTrainerRegistrations_whenCalled_shouldIncreaseCounterByOne() {
        // given
        Counter counter = meterRegistry.get("gymcrm.trainer.registrations").counter();

        // when
        gymMetrics.incrementTrainerRegistrations();

        // then
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("incrementTrainingsCreated: increase counter by one when called")
    void incrementTrainingsCreated_whenCalled_shouldIncreaseCounterByOne() {
        // given
        Counter counter = meterRegistry.get("gymcrm.training.created").counter();

        // when
        gymMetrics.incrementTrainingsCreated();
        gymMetrics.incrementTrainingsCreated();
        gymMetrics.incrementTrainingsCreated();

        // then
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("recordAuthenticationSuccess: increase success counter only when called")
    void recordAuthenticationSuccess_whenCalled_shouldIncreaseSuccessCounterOnly() {
        // given
        Counter successCounter = meterRegistry.get("gymcrm.authentication.result").tag("result", "success").counter();
        Counter failureCounter = meterRegistry.get("gymcrm.authentication.result").tag("result", "failure").counter();

        // when
        gymMetrics.recordAuthenticationSuccess();

        // then
        assertThat(successCounter.count()).isEqualTo(1.0);
        assertThat(failureCounter.count()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("recordAuthenticationFailure: increase failure counter only when called")
    void recordAuthenticationFailure_whenCalled_shouldIncreaseFailureCounterOnly() {
        // given
        Counter successCounter = meterRegistry.get("gymcrm.authentication.result").tag("result", "success").counter();
        Counter failureCounter = meterRegistry.get("gymcrm.authentication.result").tag("result", "failure").counter();

        // when
        gymMetrics.recordAuthenticationFailure();
        gymMetrics.recordAuthenticationFailure();

        // then
        assertThat(failureCounter.count()).isEqualTo(2.0);
        assertThat(successCounter.count()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("recordAuthenticationTime: execute Runnable action and record timer")
    void recordAuthenticationTime_withRunnable_shouldExecuteActionAndRecordTimer() {
        // given
        Timer timer = meterRegistry.get("gymcrm.authentication.duration").timer();
        boolean[] executed = {false};

        // when
        gymMetrics.recordAuthenticationTime((Runnable) () -> executed[0] = true);

        // then
        assertThat(executed[0]).isTrue();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("recordAuthenticationTime: execute Supplier action and record timer")
    void recordAuthenticationTime_withSupplier_shouldReturnSupplierResultAndRecordTimer() {
        // given
        Timer timer = meterRegistry.get("gymcrm.authentication.duration").timer();

        // when
        String result = gymMetrics.recordAuthenticationTime(() -> "authenticated");

        // then
        assertThat(result).isEqualTo("authenticated");
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("recordAuthenticationTime: propagate exception and still record when supplier throws exception")
    void recordAuthenticationTime_whenSupplierThrowsException_shouldPropagateExceptionAndStillRecordTimer() {
        // given
        Timer timer = meterRegistry.get("gymcrm.authentication.duration").timer();

        // when / then
        try {
            gymMetrics.recordAuthenticationTime(() -> {
                throw new RuntimeException("Authentication failed");
            });
        } catch (RuntimeException expected) {
            // expected propagation
        }

        assertThat(timer.count()).isEqualTo(1);
    }
}