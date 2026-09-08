package io.github.mrergos.gymcrm.component.steps;

import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @Autowired
    private ScenarioContext scenarioContext;

    @Before
    public void resetScenarioContext() {
        scenarioContext.reset();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @When("an unauthenticated request is made to fetch trainee {string} profile")
    public void anUnauthenticatedRequestIsMadeToFetchTraineeProfile(String username) {
        scenarioContext.setLastResponse(
                RestAssured.given().get("/api/v1/trainees/{username}", username));
    }

    @ParameterType("true|false|True|False|TRUE|FALSE")
    public Boolean myBoolean(String value) {
        return Boolean.valueOf(value);
    }
}
