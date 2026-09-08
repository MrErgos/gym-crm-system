package io.github.mrergos.fullintegration.steps;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class IntegrationScenarioContext {

    private Response lastResponse;
    private Response lastDirectWorkingHoursResponse;
    private String currentAccessToken;
    private final Map<String, String> passwordsByUsername = new HashMap<>();

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public Response getLastDirectWorkingHoursResponse() {
        return lastDirectWorkingHoursResponse;
    }

    public void setLastDirectWorkingHoursResponse(Response lastDirectWorkingHoursResponse) {
        this.lastDirectWorkingHoursResponse = lastDirectWorkingHoursResponse;
    }

    public String getCurrentAccessToken() {
        return currentAccessToken;
    }

    public void setCurrentAccessToken(String currentAccessToken) {
        this.currentAccessToken = currentAccessToken;
    }

    public void rememberPassword(String username, String password) {
        passwordsByUsername.put(username, password);
    }

    public boolean hasPassword(String username) {
        return passwordsByUsername.containsKey(username);
    }

    public String getPassword(String username) {
        String password = passwordsByUsername.get(username);
        if (password == null) {
            throw new IllegalStateException("No password remembered for username: " + username);
        }
        return password;
    }
}