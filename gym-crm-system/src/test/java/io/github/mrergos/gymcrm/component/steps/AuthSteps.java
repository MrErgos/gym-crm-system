package io.github.mrergos.gymcrm.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    private static final Logger log = LoggerFactory.getLogger(AuthSteps.class);

    @Autowired
    private ScenarioContext scenarioContext;

    @Given("the trainee logs in with their generated credentials")
    public void theTraineeLogsInWithTheirGeneratedCredentials() {
        String username = scenarioContext.getLastRegisteredUsername();
        ScenarioContext.Credentials credentials = scenarioContext.getCredentials(username);
        loginAndCaptureToken(credentials.username(), credentials.password());
    }

    @When("the trainee logs in with username {string} and password {string}")
    public void theTraineeLogsInWithUsernameAndPassword(String username, String password) {
        performLogin(username, password);
    }

    @Then("the trainee can log in with username {string} and password {string}")
    public void theTraineeCanLogInWithUsernameAndPassword(String username, String password) {
        performLogin(username, password);
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(200);
    }

    @When("the trainee attempts to log in with username {string} and an invalid password {int} times")
    public void theTraineeAttemptsToLogInWithAnInvalidPasswordNTimes(String username, int times) {
        for (int i = 0; i < times; i++) {
            performLogin(username, "TotallyWrongPass" + i);
        }
    }

    @When("the trainee changes their password to {string}")
    public void theTraineeChangesTheirPasswordTo(String newPassword) {
        Map<String, String> body = new HashMap<>();
        body.put("newPassword", newPassword);

        Response response = authenticatedRequest()
                .contentType("application/json")
                .body(body)
                .put("/api/v1/auth/password");
        scenarioContext.setLastResponse(response);
    }

    @Then("the change password response status should be {int}")
    public void theChangePasswordResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @When("the trainee logs out")
    public void theTraineeLogsOut() {
        Response response = authenticatedRequest().post("/api/v1/auth/logout");
        scenarioContext.setLastResponse(response);
    }

    @Then("the logout response status should be {int}")
    public void theLogoutResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @Then("using the revoked token to fetch the trainee's own profile returns {int}")
    public void usingTheRevokedTokenToFetchTheTraineesOwnProfileReturns(int expectedStatus) {
        String username = scenarioContext.getLastRegisteredUsername();
        Response response = authenticatedRequest().get("/api/v1/trainees/{username}", username);
        assertThat(response.statusCode()).isEqualTo(expectedStatus);
    }

    @Then("the login response status should be {int}")
    public void theLoginResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @Then("a valid access token should be returned")
    public void aValidAccessTokenShouldBeReturned() {
        String token = scenarioContext.getLastResponse().jsonPath().getString("accessToken");
        assertThat(token).isNotBlank();
    }

    private void performLogin(String username, String password) {
        String basicAuth = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8));

        log.info("Attempting login for username={}", username);
        Response response = RestAssured.given()
                .header("Authorization", "Basic " + basicAuth)
                .post("/api/v1/auth/login");
        scenarioContext.setLastResponse(response);

        if (response.statusCode() == 200) {
            scenarioContext.setCurrentAccessToken(response.jsonPath().getString("accessToken"));
        }
    }

    private void loginAndCaptureToken(String username, String password) {
        performLogin(username, password);
        assertThat(scenarioContext.getLastResponse().statusCode())
                .as("expected login to succeed for username=%s", username)
                .isEqualTo(200);
    }

    private RequestSpecification authenticatedRequest() {
        return RestAssured.given().header("Authorization", "Bearer " + scenarioContext.getCurrentAccessToken());
    }
}