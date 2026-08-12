package io.github.mrergos.gymcrm.health;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final SessionFactory sessionFactory;

    public DatabaseHealthIndicator(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public @Nullable Health health() {
        try (Session session = sessionFactory.openSession()) {
            session.createNativeQuery("select 1").getSingleResult();
            log.debug("Database health check successful");
            return Health.up()
                    .withDetail("database", "reachable")
                    .build();
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down(e)
                    .withDetail("database", "unreachable")
                    .build();
        }
    }
}
