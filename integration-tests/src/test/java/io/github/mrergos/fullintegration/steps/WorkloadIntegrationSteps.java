package io.github.mrergos.fullintegration.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.mrergos.fullintegration.config.IntegrationTestContainers;
import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadIntegrationSteps {

    private static final Logger log = LoggerFactory.getLogger(WorkloadIntegrationSteps.class);

    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(15);

    private final IntegrationScenarioContext scenarioContext;

    public WorkloadIntegrationSteps(IntegrationScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("both microservices are running and connected via Artemis broker")
    public void bothMicroservicesAreRunningAndConnected() {
        IntegrationTestContainers.restartWorkingHoursServiceIfStopped();

        Response gymCrmHealth = RestAssured.given()
                .baseUri(IntegrationTestContainers.gymCrmBaseUrl())
                .get("/actuator/health");
        Response workingHoursHealth = RestAssured.given()
                .baseUri(IntegrationTestContainers.workingHoursBaseUrl())
                .get("/actuator/health");

        assertThat(gymCrmHealth.statusCode()).isEqualTo(200);
        assertThat(workingHoursHealth.statusCode()).isEqualTo(200);
    }

    @Given("a registered active trainer with username {string} and specialization {string}")
    public void aRegisteredActiveTrainerWithUsernameAndSpecialization(String username, String specialization) {
        registerTrainerInGymCrm(username);
    }

    @Given("a registered active trainee with username {string}")
    public void aRegisteredActiveTraineeWithUsername(String username) {
        String[] parts = username.split("\\.");
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", parts[0]);
        body.put("lastName", parts.length > 1 ? parts[1] : "Trainee");

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/v1/trainees");

        assertThat(response.statusCode()).isIn(200, 201);
        scenarioContext.rememberPassword(username, response.jsonPath().getString("password"));
    }

    private void registerTrainerInGymCrm(String username) {
        String[] parts = username.split("\\.");
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", parts[0]);
        body.put("lastName", parts.length > 1 ? parts[1] : "Trainer");
        body.put("specializationId", 2L);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/v1/trainers");

        assertThat(response.statusCode()).isIn(200, 201);
        scenarioContext.rememberPassword(username, response.jsonPath().getString("password"));
    }

    @Given("trainer {string} has no workload data in Working Hours Service yet")
    public void trainerHasNoWorkloadDataYet(String trainerUsername) {
        Response response = RestAssured.given()
                .baseUri(IntegrationTestContainers.workingHoursBaseUrl())
                .header("Authorization", "Bearer " + serviceToServiceToken())
                .get("/api/v1/trainers/{username}/workload", trainerUsername);

        assertThat(response.statusCode())
                .as("expected no pre-existing workload data for trainer=%s; " +
                        "pick a fresh username or this precondition doesn't hold", trainerUsername)
                .isEqualTo(404);
    }

    private String loginAndGetAccessToken(String username) {
        String password = scenarioContext.getPassword(username);
        String basicAuth = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8));

        Response loginResponse = RestAssured.given()
                .header("Authorization", "Basic " + basicAuth)
                .post("/api/v1/auth/login");

        assertThat(loginResponse.statusCode())
                .as("expected login to succeed for username=%s", username)
                .isEqualTo(200);

        return loginResponse.jsonPath().getString("accessToken");
    }

    @When("the user creates a training named {string} for trainee {string} and trainer {string} with date {string}, duration {int}, type {string}")
    public void theUserCreatesATraining(String trainingName, String traineeUsername, String trainerUsername,
                                        String date, int duration, String type) {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", traineeUsername);
        body.put("trainerUsername", trainerUsername);
        body.put("trainingName", trainingName);
        body.put("trainingDate", date);
        body.put("trainingDuration", duration);

        String accessToken = loginAndGetAccessToken(traineeUsername);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/v1/trainings");

        scenarioContext.setLastResponse(response);
    }


    @Given("Working Hours Service is stopped")
    public void workingHoursServiceIsStopped() {
        log.info("Simulating outage: stopping working-hours-service container");
        IntegrationTestContainers.stopWorkingHoursService();
    }

    @When("Working Hours Service is started again")
    public void workingHoursServiceIsStartedAgain() {
        log.info("Ending simulated outage: restarting working-hours-service container");
        IntegrationTestContainers.restartWorkingHoursServiceIfStopped();

        Awaitility.await()
                .atMost(AWAIT_TIMEOUT)
                .untilAsserted(() -> {
                    Response health = RestAssured.given()
                            .baseUri(IntegrationTestContainers.workingHoursBaseUrl())
                            .get("/actuator/health");
                    assertThat(health.statusCode()).isEqualTo(200);
                });
    }


    @Then("the creation response status should be 200 or 201")
    public void theCreationResponseStatusShouldBe200Or201() {
        assertThat(scenarioContext.getLastResponse().statusCode()).isIn(200, 201);
    }

    @Then("eventually trainer {string} should have {int} minutes recorded for year {int} month {int} in Working Hours Service")
    public void eventuallyTrainerShouldHaveMinutesRecorded(String trainerUsername, int expectedMinutes,
                                                           int year, int month) {
        Awaitility.await()
                .atMost(AWAIT_TIMEOUT)
                .untilAsserted(() -> {
                    Response response = RestAssured.given()
                            .baseUri(IntegrationTestContainers.workingHoursBaseUrl())
                            .header("Authorization", "Bearer " + serviceToServiceToken())
                            .get("/api/v1/trainers/{username}/workload", trainerUsername);

                    assertThat(response.statusCode()).isEqualTo(200);
                    Integer minutes = response.jsonPath().getInt(String.format(
                            "years.find { it.year == %d }.months.find { it.month == %d }.trainingSummaryDuration",
                            year, month));
                    assertThat(minutes).isEqualTo(expectedMinutes);
                });
    }

    private String serviceToServiceToken() {
        return Jwts.builder()
                .subject("gym-crm-system")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "integration-test-secret-key-must-be-at-least-256-bits-long!!"
                                .getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}