package io.github.mrergos.workinghours.component.config;

import io.cucumber.spring.CucumberContextConfiguration;
import io.github.mrergos.workinghours.WorkingHoursServiceApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@Testcontainers
@SpringBootTest(
        classes = WorkingHoursServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("component-test")
public class CucumberSpringConfiguration {

    private static final String ARTEMIS_IMAGE = "apache/activemq-artemis:2.37.0";
    private static final String ARTEMIS_USER = "artemis";
    private static final String ARTEMIS_PASSWORD = "artemis";

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    static GenericContainer<?> artemisContainer = new GenericContainer<>(DockerImageName.parse(ARTEMIS_IMAGE))
            .withExposedPorts(61616, 8161)
            .withEnv("ARTEMIS_USER", ARTEMIS_USER)
            .withEnv("ARTEMIS_PASSWORD", ARTEMIS_PASSWORD)
            .withEnv("ANONYMOUS_LOGIN", "false")
            .waitingFor(Wait.forListeningPort());

    static {
        artemisContainer.start();
    }

    @DynamicPropertySource
    static void artemisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.artemis.mode", () -> "native");
        registry.add("spring.artemis.broker-url",
                () -> String.format("tcp://%s:%d", artemisContainer.getHost(), artemisContainer.getMappedPort(61616)));
        registry.add("spring.artemis.user", () -> ARTEMIS_USER);
        registry.add("spring.artemis.password", () -> ARTEMIS_PASSWORD);
    }
}
