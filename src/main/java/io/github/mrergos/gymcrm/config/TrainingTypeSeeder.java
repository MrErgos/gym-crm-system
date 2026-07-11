package io.github.mrergos.gymcrm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TrainingTypeSeeder implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeSeeder.class);

    private final DataSource dataSource;
    private final Resource dataSqlResource;
    private volatile boolean running = false;

    public TrainingTypeSeeder(DataSource dataSource,
                              @Value("classpath:data.sql") Resource dataSqlResource) {
        this.dataSource = dataSource;
        this.dataSqlResource = dataSqlResource;
    }

    @Override
    public void start() {
        log.info("Seeding constant training_types table from data.sql");
        String script = readScript();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String rawStatement : script.split(";")) {
                String sql = rawStatement.strip();
                if (sql.isEmpty() || sql.startsWith("--")) {
                    continue;
                }
                statement.execute(sql);
            }
            log.info("training_types seeding completed successfully");
        } catch (SQLException e) {
            log.error("Failed to seed training_types table", e);
            throw new IllegalStateException("Could not seed training_types table", e);
        }
        running = true;
    }

    private String readScript() {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(dataSqlResource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.strip().startsWith("--")) {
                    continue;
                }
                builder.append(line).append('\n');
            }
        } catch (IOException e) {
            log.error("Failed to read data.sql", e);
            throw new IllegalStateException("Could not read data.sql", e);
        }
        return builder.toString();
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }
}

