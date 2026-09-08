package io.github.mrergos.fullintegration.config;

import io.cucumber.java.Before;
import io.restassured.RestAssured;

public class CucumberIntegrationConfiguration {

    @Before
    public void pointRestAssuredAtGymCrm() {
        RestAssured.baseURI = IntegrationTestContainers.gymCrmBaseUrl();
        RestAssured.port = RestAssured.UNDEFINED_PORT;
    }
}