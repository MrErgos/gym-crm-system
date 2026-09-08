package io.github.mrergos.gymcrm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {
    private static final Logger log = LoggerFactory.getLogger(GymMetrics.class);

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsCreated;
    private final Counter authenticationSuccess;
    private final Counter authenticationFailure;
    private final Timer authenticationTimer;

    public GymMetrics(MeterRegistry registry) {
        this.traineeRegistrations = Counter.builder("gymcrm.trainee.registrations")
                .description("Number of trainee profiles created")
                .register(registry);
        this.trainerRegistrations = Counter.builder("gymcrm.trainer.registrations")
                .description("Number of trainer profiles created")
                .register(registry);
        this.trainingsCreated = Counter.builder("gymcrm.training.created")
                .description("Number of trainings created")
                .register(registry);
        this.authenticationSuccess = Counter.builder("gymcrm.authentication.result")
                .tag("result", "success")
                .description("Authentication result")
                .register(registry);
        this.authenticationFailure = Counter.builder("gymcrm.authentication.result")
                .tag("result", "failure")
                .description("Authentication result")
                .register(registry);
        this.authenticationTimer = Timer.builder("gymcrm.authentication.duration")
                .description("Time spent authenticating a user")
                .register(registry);

        log.info("GymMetrics initialized");
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrations.increment();
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrations.increment();
    }

    public void incrementTrainingsCreated() {
        trainingsCreated.increment();
    }

    public void recordAuthenticationSuccess() {
        authenticationSuccess.increment();
    }

    public void recordAuthenticationFailure() {
        authenticationFailure.increment();
    }

    public <T> T recordAuthenticationTime(java.util.function.Supplier<T> action) {
        return authenticationTimer.record(action);
    }

    public void recordAuthenticationTime(Runnable action) {
        authenticationTimer.record(action);
    }
}
