package io.github.mrergos.gymcrm.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.mrergos.gymcrm.dto.request.CreateTrainingRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

public class TrainingSteps {

    private static final Logger log = LoggerFactory.getLogger(TrainingSteps.class);

    @Autowired
    private ScenarioContext scenarioContext;

    private RequestSpecification request() {
        RequestSpecification req = RestAssured.given().contentType(ContentType.JSON);
        if (scenarioContext.getCurrentAccessToken() != null && !scenarioContext.getCurrentAccessToken().isBlank()) {
            req.header("Authorization", "Bearer " + scenarioContext.getCurrentAccessToken());
        }
        return req;
    }


    @Given("a training named {string} is created for trainee {string} and trainer {string} with date {string} and duration {int}")
    @When("the user creates a training named {string} for trainee {string} and trainer {string} with date {string} and duration {int}")
    public void createTraining(String trainingName, String traineeUsername, String trainerUsername, String date, int duration) {
        log.info("Creating training '{}' for trainee '{}' and trainer '{}'", trainingName, traineeUsername, trainerUsername);
        CreateTrainingRequest req = new CreateTrainingRequest(
                traineeUsername,
                trainerUsername,
                trainingName,
                LocalDate.parse(date),
                duration
        );

        Response response = request()
                .body(req)
                .post("/api/v1/trainings");

        scenarioContext.setLastResponse(response);
    }

    @When("the user creates a training named {string} for trainee {string} and a non-existent trainer {string} with date {string} and duration {int}")
    public void createTrainingWithMissingTrainer(String trainingName, String traineeUsername, String trainerUsername, String date, int duration) {
        createTraining(trainingName, traineeUsername, trainerUsername, date, duration);
    }

    @When("the user creates a training named {string} for a non-existent trainee {string} and trainer {string} with date {string} and duration {int}")
    public void createTrainingWithMissingTrainee(String trainingName, String traineeUsername, String trainerUsername, String date, int duration) {
        createTraining(trainingName, traineeUsername, trainerUsername, date, duration);
    }

    @When("the user creates a training named {string} for trainee {string} and trainer {string} with date {string} and a non-positive duration {int}")
    public void createTrainingWithInvalidDuration(String trainingName, String traineeUsername, String trainerUsername, String date, int duration) {
        createTraining(trainingName, traineeUsername, trainerUsername, date, duration);
    }


    @When("the user fetches trainings for trainee {string}")
    public void fetchTrainingsForTrainee(String username) {
        log.info("Fetching trainings for trainee: {}", username);
        Response response = request().get("/api/v1/trainings/trainee/" + username);
        scenarioContext.setLastResponse(response);
    }

    @When("the user fetches trainings for trainee {string} with date range from {string} to {string}, trainer {string}, type {string}")
    public void fetchTrainingsForTraineeFiltered(String username, String fromDate, String toDate, String trainerName, String type) {
        log.info("Fetching filtered trainings for trainee: {}", username);
        RequestSpecification req = request();
        if (fromDate != null && !fromDate.isBlank()) req.queryParam("fromDate", fromDate);
        if (toDate != null && !toDate.isBlank()) req.queryParam("toDate", toDate);
        if (trainerName != null && !trainerName.isBlank()) req.queryParam("trainerName", trainerName);
        if (type != null && !type.isBlank()) req.queryParam("trainingTypeName", type);

        Response response = req.get("/api/v1/trainings/trainee/" + username);
        scenarioContext.setLastResponse(response);
    }


    @When("the user fetches trainings for trainer {string}")
    public void fetchTrainingsForTrainer(String username) {
        log.info("Fetching trainings for trainer: {}", username);
        Response response = request().get("/api/v1/trainings/trainer/" + username);
        scenarioContext.setLastResponse(response);
    }

    @Then("the training response list should contain training named {string}")
    public void verifyTrainingsListContains(String trainingName) {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("trainingName", hasItem(trainingName));
    }

    @Then("the training response list should be empty")
    public void verifyTrainingsListEmpty() {
        scenarioContext.getLastResponse().then()
                .statusCode(200)
                .body("$", empty());
    }

    @Then("the created training response should contain trainee {string}, trainer {string} and training type {string}")
    public void verifyCreatedTrainingDetails(String traineeUsername, String trainerUsername, String trainingTypeName) {
        scenarioContext.getLastResponse().then()
                .statusCode(201)
                .body("traineeUsername", equalTo(traineeUsername))
                .body("trainerUsername", equalTo(trainerUsername))
                .body("trainingTypeName", equalTo(trainingTypeName));
    }
}