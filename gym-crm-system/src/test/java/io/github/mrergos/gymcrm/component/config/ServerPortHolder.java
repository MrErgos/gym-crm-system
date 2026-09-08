package io.github.mrergos.gymcrm.component.config;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.boot.test.web.server.LocalServerPort;

public class ServerPortHolder {
    @LocalServerPort
    private int port;

    @Before
    public void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}
