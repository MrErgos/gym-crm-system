package io.github.mrergos.workinghours.component.steps;

import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryReply;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class ScenarioContext {

    private Response lastResponse;
    private WorkloadSummaryReply lastJmsReply;
    private boolean authenticated = true;

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public WorkloadSummaryReply getLastJmsReply() {
        return lastJmsReply;
    }

    public void setLastJmsReply(WorkloadSummaryReply lastJmsReply) {
        this.lastJmsReply = lastJmsReply;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void reset() {
        lastResponse = null;
        lastJmsReply = null;
        authenticated = true;
    }
}
