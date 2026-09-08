package io.github.mrergos.gymcrm.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.mrergos.gymcrm.dto.request.RegisterTrainerRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTrainerRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;

public class TrainerSteps {

    private static final Logger log = LoggerFactory.getLogger(TrainerSteps.class);

    @Autowired
    private ScenarioContext scenarioContext;

    private RequestSpecification request() {
        RequestSpecification req = RestAssured.given().contentType(ContentType.JSON);
        if (scenarioContext.getCurrentAccessToken() != null && !scenarioContext.getCurrentAccessToken().isBlank()) {
            req.header("Authorization", "Bearer " + scenarioContext.getCurrentAccessToken());
        }
        return req;
    }

    @Given("a trainer is registered with first name {string}, last name {string}, specialization id {long}")
    @When("the user registers a trainer with first name {string}, last name {string}, specialization id {long}")
    public void registerTrainer(String firstName, String lastName, Long specializationId) {
        log.info("Registering trainer: {} {} with specializationId {}", firstName, lastName, specializationId);
        RegisterTrainerRequest req = new RegisterTrainerRequest(firstName, lastName, specializationId);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(req)
                .post("/api/v1/trainers");

        scenarioContext.setLastResponse(response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            String username = response.jsonPath().getString("username");
            String password = response.jsonPath().getString("password");
            scenarioContext.rememberCredentials(username, password);
            scenarioContext.setLastRegisteredUsername(username);
            log.info("Trainer registered with username: {}", username);
        }
    }

    @When("the user registers a trainer with an empty first name and last name {string} and specialization id {long}")
    public void registerTrainerWithEmptyFirstName(String lastName, Long specializationId) {
        log.info("Registering trainer with invalid (empty) first name, lastName={}", lastName);
        RegisterTrainerRequest req = new RegisterTrainerRequest("", lastName, specializationId);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(req)
                .post("/api/v1/trainers");

        scenarioContext.setLastResponse(response);
    }

    @When("the user registers a trainer with first name {string}, last name {string} and no specialization")
    public void registerTrainerWithoutSpecialization(String firstName, String lastName) {
        log.info("Registering trainer with missing specializationId: {} {}", firstName, lastName);
        RegisterTrainerRequest req = new RegisterTrainerRequest(firstName, lastName, null);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(req)
                .post("/api/v1/trainers");

        scenarioContext.setLastResponse(response);
    }

    @When("the trainer fetches their own profile")
    public void fetchOwnProfile() {
        String username = scenarioContext.getLastRegisteredUsername();
        fetchTrainerProfile(username);
    }

    @When("the user fetches trainer profile for {string}")
    public void fetchTrainerProfile(String username) {
        log.info("Fetching profile for trainer: {}", username);
        Response response = request().get("/api/v1/trainers/" + username);
        scenarioContext.setLastResponse(response);
    }

    @When("the trainer updates their profile with first name {string}, last name {string}, specialization id {long}, isActive {myBoolean}")
    public void updateOwnTrainerProfile(String firstName, String lastName, Long specializationId, Boolean isActive) {
        String username = scenarioContext.getLastRegisteredUsername();
        updateTrainerProfile(username, firstName, lastName, specializationId, isActive);
    }

    @When("the user updates profile for trainer {string} with first name {string}, last name {string}, specialization id {long}, isActive {myBoolean}")
    public void updateTrainerProfile(String username, String firstName, String lastName, Long specializationId, Boolean isActive) {
        log.info("Updating profile for trainer: {}", username);
        UpdateTrainerRequest req = new UpdateTrainerRequest(firstName, lastName, specializationId, isActive);

        Response response = request()
                .body(req)
                .put("/api/v1/trainers/" + username);

        scenarioContext.setLastResponse(response);
    }

    @When("the user toggles active status for trainer {string}")
    public void toggleActiveStatus(String username) {
        log.info("Toggling active status for trainer {}", username);
        Response response = request()
                .patch("/api/v1/trainers/" + username + "/status");

        scenarioContext.setLastResponse(response);
    }

    @When("the user fetches available training types")
    public void fetchAvailableTrainingTypes() {
        log.info("Fetching available training types");
        Response response = request().get("/api/v1/trainers/training-types");
        scenarioContext.setLastResponse(response);
    }

    @Then("the trainer profile response should contain first name {string} and last name {string}")
    public void verifyTrainerProfileNames(String firstName, String lastName) {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName));
    }

    @Then("the trainer's active status should be {myBoolean}")
    public void verifyTrainerActiveStatus(Boolean expectedActive) {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("isActive", equalTo(expectedActive));
    }
}