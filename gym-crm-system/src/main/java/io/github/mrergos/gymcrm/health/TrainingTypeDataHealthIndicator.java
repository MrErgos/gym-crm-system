package io.github.mrergos.gymcrm.health;

import io.github.mrergos.gymcrm.dao.TrainingTypeDao;
import io.github.mrergos.gymcrm.entity.TrainingType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component("trainingTypesData")
public class TrainingTypeDataHealthIndicator implements HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDataHealthIndicator.class);

    private final TrainingTypeDao trainingTypeDao;
    private final TransactionTemplate transactionTemplate;

    public TrainingTypeDataHealthIndicator(TrainingTypeDao trainingTypeDao, TransactionTemplate transactionTemplate) {
        this.trainingTypeDao = trainingTypeDao;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public @Nullable Health health() {
        try {
            List<TrainingType> types = transactionTemplate.execute(status -> trainingTypeDao.findAll());
            int count = types == null ? 0 : types.size();

            if (count == 0) {
                log.warn("training_types table is empty. " +
                        "Trainer registration will fail until reference data is present (check data.sql / active profile)");
                return Health.down()
                        .withDetail("trainingTypesCount", 0)
                        .withDetail("reason", "training_type table is empty")
                        .build();
            }
            log.debug("training_types table contains {} types", count);
            return Health.up()
                    .withDetail("trainingTypesCount", count)
                    .build();
        } catch (Exception e) {
            log.error("Database Training Type references check failed", e);
            return Health.down(e)
                    .build();
        }
    }
}
