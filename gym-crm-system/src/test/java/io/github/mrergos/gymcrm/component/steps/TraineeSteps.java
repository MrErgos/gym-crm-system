package io.github.mrergos.gymcrm.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.mrergos.gymcrm.dto.request.RegisterTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

public class TraineeSteps {

    private static final Logger log = LoggerFactory.getLogger(TraineeSteps.class);

    @Autowired
    private ScenarioContext scenarioContext;

    private RequestSpecification request() {
        RequestSpecification req = RestAssured.given().contentType(ContentType.JSON);
        if (scenarioContext.getCurrentAccessToken() != null && !scenarioContext.getCurrentAccessToken().isBlank()) {
            req.header("Authorization", "Bearer " + scenarioContext.getCurrentAccessToken());
        }
        return req;
    }

    @Given("a trainee is registered with first name {string}, last name {string}, date of birth {string}, address {string}")
    @When("the user registers a trainee with first name {string}, last name {string}, date of birth {string}, address {string}")
    public void registerTrainee(String firstName, String lastName, String dob, String address) {
        log.info("Registering trainee: {} {}", firstName, lastName);
        LocalDate parsedDob = (dob != null && !dob.isBlank()) ? LocalDate.parse(dob) : null;
        doRegisterTrainee(firstName, lastName, parsedDob, address);
    }

    @Given("a trainee is registered with first name {string} and last name {string}")
    public void registerTraineeShort(String firstName, String lastName) {
        log.info("Registering trainee (short form): {} {}", firstName, lastName);
        doRegisterTrainee(firstName, lastName, null, null);
    }

    private void doRegisterTrainee(String firstName, String lastName, LocalDate dob, String address) {
        RegisterTraineeRequest req = new RegisterTraineeRequest(firstName, lastName, dob, address);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(req)
                .post("/api/v1/trainees");

        scenarioContext.setLastResponse(response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            String username = response.jsonPath().getString("username");
            String password = response.jsonPath().getString("password");
            scenarioContext.rememberCredentials(username, password);
            scenarioContext.setLastRegisteredUsername(username);
            log.info("Trainee registered with username: {}", username);
        }
    }

    @When("the user registers a trainee with an empty first name and last name {string}")
    public void registerTraineeWithEmptyFirstName(String lastName) {
        log.info("Registering trainee with invalid (empty) first name, lastName={}", lastName);
        doRegisterTrainee("", lastName, null, null);
    }

    @When("the trainee fetches their own profile")
    public void fetchOwnProfile() {
        String username = scenarioContext.getLastRegisteredUsername();
        fetchTraineeProfile(username);
    }

    @When("the user fetches trainee profile for {string}")
    public void fetchTraineeProfile(String username) {
        log.info("Fetching profile for trainee: {}", username);
        Response response = request().get("/api/v1/trainees/" + username);
        scenarioContext.setLastResponse(response);
    }

    @When("the trainee updates their profile with first name {string}, last name {string}, date of birth {string}, address {string}, isActive {myBoolean}")
    public void updateTraineeProfile(String firstName, String lastName, String dob, String address, Boolean isActive) {
        String username = scenarioContext.getLastRegisteredUsername();
        updateTraineeProfileForUser(username, firstName, lastName, dob, address, isActive);
    }

    @When("the user updates profile for trainee {string} with first name {string}, last name {string}, date of birth {string}, address {string}, isActive {myBoolean}")
    public void updateTraineeProfileForUser(String username, String firstName, String lastName, String dob, String address, Boolean isActive) {
        log.info("Updating profile for trainee: {}", username);
        LocalDate parsedDob = (dob != null && !dob.isBlank()) ? LocalDate.parse(dob) : null;
        UpdateTraineeRequest req = new UpdateTraineeRequest(firstName, lastName, parsedDob, address, isActive);

        Response response = request()
                .body(req)
                .put("/api/v1/trainees/" + username);

        scenarioContext.setLastResponse(response);
    }

    @When("the trainee deletes profile for {string}")
    public void deleteTraineeProfile(String username) {
        log.info("Deleting trainee profile: {}", username);
        Response response = request().delete("/api/v1/trainees/" + username);
        scenarioContext.setLastResponse(response);
    }

    @When("the trainee deletes their own profile")
    public void deleteOwnProfile() {
        deleteTraineeProfile(scenarioContext.getLastRegisteredUsername());
    }

    @When("the user fetches unassigned trainers for trainee {string}")
    public void fetchUnassignedTrainers(String username) {
        log.info("Fetching unassigned trainers for trainee: {}", username);
        Response response = request().get("/api/v1/trainees/" + username + "/trainers/available");
        scenarioContext.setLastResponse(response);
    }

    @When("the user toggles isActive status for trainee {string}")
    public void toggleActiveStatus(String username) {
        log.info("Toggling isActive status for trainee {}", username);
        Response response = request()
                .patch("/api/v1/trainees/" + username + "/status");

        scenarioContext.setLastResponse(response);
    }

    @Then("the trainee profile response should contain first name {string} and last name {string}")
    public void verifyTraineeProfileNames(String firstName, String lastName) {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName));
    }

    @Then("the trainee's isActive status should be {myBoolean}")
    public void verifyTraineeActiveStatus(Boolean expectedActive) {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("isActive", equalTo(expectedActive));
    }

    @Then("the unassigned trainers list should be empty")
    public void verifyUnassignedTrainersEmpty() {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("$", empty());
    }

    @Then("the registration response should contain generated credentials")
    public void verifyRegistrationCredentials() {
        scenarioContext.getLastResponse().then()
                .statusCode(201)
                .body("username", not(emptyOrNullString()))
                .body("password", not(emptyOrNullString()));
    }
}