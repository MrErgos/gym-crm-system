package io.github.mrergos.fullintegration.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public final class IntegrationTestContainers {

    private static final Logger log = LoggerFactory.getLogger(IntegrationTestContainers.class);

    private static final String PROJECT_NAME = "gym-crm-integration-test";

    private static final ComposeContainer ENVIRONMENT;

    static {
        log.info("Bootstrapping full-integration Docker Compose environment");

        File repoRoot = new File(System.getProperty("repo.root", ".."));
        File composeFile = new File(repoRoot, "docker-compose.integration-test.yml");

        ENVIRONMENT = new ComposeContainer(PROJECT_NAME, composeFile)
                .withLocalCompose(true)
                .withBuild(true)
                .withExposedService("working-hours-service", 8082,
                        Wait.forHttp("/actuator/health").forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(2)))
                .withExposedService("gym-crm-system", 8080,
                        Wait.forHttp("/actuator/health").forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(2)));
        ENVIRONMENT.start();

        log.info("gym-crm-system reachable at {}", gymCrmBaseUrl());
        log.info("working-hours-service reachable at {}", workingHoursBaseUrl());
    }

    private IntegrationTestContainers() {
    }

    public static String gymCrmBaseUrl() {
        return "http://" + ENVIRONMENT.getServiceHost("gym-crm-system", 8080)
                + ":" + ENVIRONMENT.getServicePort("gym-crm-system", 8080);
    }

    public static String workingHoursBaseUrl() {
        return "http://" + ENVIRONMENT.getServiceHost("working-hours-service", 8082)
                + ":" + ENVIRONMENT.getServicePort("working-hours-service", 8082);
    }

    public static void stopWorkingHoursService() {
        log.warn("Stopping working-hours-service container to simulate an outage");
        runComposeCommand("stop", "working-hours-service");
    }

    public static void restartWorkingHoursServiceIfStopped() {
        if (!isWorkingHoursServiceRunning()) {
            log.info("Restarting working-hours-service container after simulated outage");
            runComposeCommand("start", "working-hours-service");
        }
    }

    private static boolean isWorkingHoursServiceRunning() {
        File repoRoot = new File(System.getProperty("repo.root", ".."));
        File composeFile = new File(repoRoot, "docker-compose.integration-test.yml");

        var command = List.of(
                "docker", "compose", "-f", composeFile.getAbsolutePath(), "-p", PROJECT_NAME,
                "ps", "--status", "running", "--services");

        try {
            Process process = new ProcessBuilder(command)
                    .directory(repoRoot)
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor(10, TimeUnit.SECONDS);
            return output.lines().anyMatch(line -> line.trim().equals("working-hours-service"));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to check working-hours-service status", e);
        }
    }

    private static void runComposeCommand(String... args) {
        File repoRoot = new File(System.getProperty("repo.root", ".."));
        File composeFile = new File(repoRoot, "docker-compose.integration-test.yml");

        var command = new ArrayList<String>();
        command.add("docker");
        command.add("compose");
        command.add("-f");
        command.add(composeFile.getAbsolutePath());
        command.add("-p");
        command.add(PROJECT_NAME);
        command.addAll(List.of(args));

        try {
            Process process = new ProcessBuilder(command)
                    .directory(repoRoot)
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                throw new IllegalStateException(
                        "docker compose command failed (args=" + Arrays.toString(args) + "): " + output);
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to run docker compose command: " + Arrays.toString(args), e);
        }
    }
}