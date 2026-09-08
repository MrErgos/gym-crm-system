package io.github.mrergos.gymcrm.component.steps;

import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class ScenarioContext {

    private Response lastResponse;
    private String currentAccessToken;
    private String lastRegisteredUsername;
    private final Map<String, Credentials> credentialsByUsername = new HashMap<>();

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getCurrentAccessToken() {
        return currentAccessToken;
    }

    public void setCurrentAccessToken(String currentAccessToken) {
        this.currentAccessToken = currentAccessToken;
    }

    public void rememberCredentials(String username, String password) {
        credentialsByUsername.put(username, new Credentials(username, password));
    }

    public Credentials getCredentials(String username) {
        Credentials credentials = credentialsByUsername.get(username);
        if (credentials == null) {
            throw new IllegalStateException("No credentials remembered for username: " + username);
        }
        return credentials;
    }

    public String getLastRegisteredUsername() {
        return lastRegisteredUsername;
    }

    public void setLastRegisteredUsername(String lastRegisteredUsername) {
        this.lastRegisteredUsername = lastRegisteredUsername;
    }

    public void reset() {
        lastResponse = null;
        currentAccessToken = null;
        lastRegisteredUsername = null;
        credentialsByUsername.clear();
    }

    public record Credentials(String username, String password) {
    }
}
