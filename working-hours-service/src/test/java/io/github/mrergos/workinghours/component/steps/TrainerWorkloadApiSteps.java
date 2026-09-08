package io.github.mrergos.workinghours.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.mrergos.workinghours.component.config.MongoCleaner;
import io.github.mrergos.workinghours.component.config.ServerPortHolder;
import io.github.mrergos.workinghours.component.config.TestJwtIssuer;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWorkloadApiSteps {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadApiSteps.class);

    @Autowired
    private ServerPortHolder serverPortHolder;

    @Autowired
    private MongoCleaner mongoCleaner;

    @Autowired
    private TestJwtIssuer testJwtIssuer;

    @Autowired
    private ScenarioContext scenarioContext;

    private String bearerToken;

    @Given("the working-hours-service is running with a clean database")
    public void theServiceIsRunningWithACleanDatabase() {
        mongoCleaner.clean();
        log.info("Component test scenario starting against port={}, database cleaned", serverPortHolder.getPort());
    }

    @Given("I am authenticated as a trusted service caller")
    public void iAmAuthenticatedAsATrustedServiceCaller() {
        this.bearerToken = testJwtIssuer.issueValidToken("gym-crm-system");
    }

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        this.bearerToken = null;
    }

    @Given("trainer {string} has no recorded workload")
    public void trainerHasNoRecordedWorkload(String trainerUsername) {
        log.debug("Precondition: trainer {} starts with no workload data", trainerUsername);
    }

    @When("I submit a workload event for trainer {string} with action {string}, date {string} and duration {int} minutes")
    public void iSubmitAWorkloadEvent(String trainerUsername, String action, String date, int duration) {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", trainerUsername);
        body.put("trainerFirstName", trainerUsername.split("\\.")[0]);
        body.put("trainerLastName", trainerUsername.contains(".") ? trainerUsername.split("\\.")[1] : "Unknown");
        body.put("isActive", true);
        body.put("trainingDate", date);
        body.put("trainingDuration", duration);
        body.put("actionType", action);

        var request = RestAssured.given().contentType("application/json").body(body);
        if (bearerToken != null) {
            request = request.header("Authorization", "Bearer " + bearerToken);
        }

        log.info("Submitting workload event via REST: trainer={}, action={}", trainerUsername, action);
        scenarioContext.setLastResponse(request.post("/api/v1/trainers/workload"));
    }

    @When("I request the workload summary for trainer {string}")
    public void iRequestTheWorkloadSummaryForTrainer(String trainerUsername) {
        var request = RestAssured.given();
        if (bearerToken != null) {
            request = request.header("Authorization", "Bearer " + bearerToken);
        }
        scenarioContext.setLastResponse(request.get("/api/v1/trainers/{username}/workload", trainerUsername));
    }

    @Then("the workload event should be applied successfully")
    public void theWorkloadEventShouldBeAppliedSuccessfully() {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(200);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @Then("the response error message should mention {string}")
    public void theResponseErrorMessageShouldMention(String expectedFragment) {
        String message = scenarioContext.getLastResponse().jsonPath().getString("message");
        assertThat(message).contains(expectedFragment);
    }

    @Then("the workload summary for trainer {string} for year {int} month {int} should show {int} minutes")
    public void theWorkloadSummaryShouldShowMinutes(String trainerUsername, int year, int month, int expectedMinutes) {
        var request = RestAssured.given().header("Authorization", "Bearer " + bearerToken);
        Response response = request.get("/api/v1/trainers/{username}/workload", trainerUsername);

        assertThat(response.statusCode()).isEqualTo(200);

        Integer actualMinutes = response.jsonPath()
                .getInt(String.format("years.find { it.year == %d }.months.find { it.month == %d }.trainingSummaryDuration",
                        year, month));

        assertThat(actualMinutes)
                .as("training duration for trainer=%s year=%d month=%d", trainerUsername, year, month)
                .isEqualTo(expectedMinutes);
    }
}
