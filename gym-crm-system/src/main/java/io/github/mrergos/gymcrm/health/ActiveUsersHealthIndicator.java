package io.github.mrergos.gymcrm.health;


import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component("activeUsers")
public class ActiveUsersHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ActiveUsersHealthIndicator.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TransactionTemplate transactionTemplate;

    public ActiveUsersHealthIndicator(TraineeDao traineeDao, TrainerDao trainerDao,
                                      TransactionTemplate transactionTemplate) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Health health() {
        try {
            long[] counts = transactionTemplate.execute(status -> new long[]{
                    traineeDao.findAll().stream().filter(User::isActive).count(),
                    trainerDao.findAll().stream().filter(User::isActive).count()
            });

            log.debug("Active trainees={}, active trainers={}", counts[0], counts[1]);
            return Health.up()
                    .withDetail("activeTrainees", counts[0])
                    .withDetail("activeTrainers", counts[1])
                    .build();
        } catch (Exception e) {
            log.error("Failed to compute active users health", e);
            return Health.down(e).build();
        }
    }
}
