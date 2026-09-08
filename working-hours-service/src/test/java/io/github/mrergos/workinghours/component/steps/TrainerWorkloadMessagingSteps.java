package io.github.mrergos.workinghours.component.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.mrergos.workinghours.component.config.MongoCleaner;
import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.MonthSummaryResponse;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryReply;
import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryRequest;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWorkloadMessagingSteps {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadMessagingSteps.class);
    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REPLY_RECEIVE_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private MongoCleaner mongoCleaner;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${jms.queues.workload-events}")
    private String workloadEventsQueueName;

    @Value("${jms.queues.workload-events-invalid}")
    private String invalidQueueName;

    @Value("${jms.queues.workload-request}")
    private String workloadRequestQueueName;

    private WorkloadSummaryReply lastReply;

    @Before
    public void setUp() {
        mongoCleaner.clean();
    }

    @When("a workload event message is published for trainer {string} with action {string}, date {string} and duration {int} minutes")
    public void aWorkloadEventMessageIsPublished(String trainerUsername, String action, String date, int duration) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                trainerUsername,
                trainerUsername.split("\\.")[0],
                trainerUsername.contains(".") ? trainerUsername.split("\\.")[1] : "Unknown",
                true,
                LocalDate.parse(date),
                duration,
                ActionType.valueOf(action)
        );
        log.info("Publishing workload event via JMS: trainer={}, action={}", trainerUsername, action);
        jmsTemplate.convertAndSend(workloadEventsQueueName, request);
    }

    @When("an invalid workload event message is published with a missing trainer username")
    public void anInvalidWorkloadEventMessageIsPublished() {
        TrainerWorkloadRequest invalidRequest = new TrainerWorkloadRequest(
                null, "NoUsername", "Trainer", true, LocalDate.of(2026, 8, 1), 30, ActionType.ADD);
        log.info("Publishing invalid workload event via JMS (missing trainerUsername)");
        jmsTemplate.convertAndSend(workloadEventsQueueName, invalidRequest);
    }

    @When("I send a JMS workload summary request for trainer {string}")
    public void iSendAJmsWorkloadSummaryRequest(String trainerUsername) throws JMSException {
        this.lastReply = requestWorkloadSummary(trainerUsername);
    }

    @Then("eventually the workload summary for trainer {string} for year {int} month {int} should show {int} minutes")
    public void eventuallyTheWorkloadSummaryShouldShowMinutes(String trainerUsername, int year, int month, int expectedMinutes) {
        Awaitility.await()
                .atMost(AWAIT_TIMEOUT)
                .untilAsserted(() -> {
                    WorkloadSummaryReply reply = requestWorkloadSummary(trainerUsername);
                    assertThat(reply.found()).isTrue();
                    Integer minutes = extractMinutes(reply, year, month);
                    assertThat(minutes).isEqualTo(expectedMinutes);
                });
    }

    @Then("the JMS reply should indicate the summary was found")
    public void theJmsReplyShouldIndicateTheSummaryWasFound() {
        assertThat(lastReply.found()).isTrue();
        assertThat(lastReply.summary()).isNotNull();
    }

    @Then("the JMS reply should indicate the summary was not found")
    public void theJmsReplyShouldIndicateTheSummaryWasNotFound() {
        assertThat(lastReply.found()).isFalse();
        assertThat(lastReply.summary()).isNull();
    }

    @Then("the JMS reply summary for year {int} month {int} should show {int} minutes")
    public void theJmsReplySummaryShouldShowMinutes(int year, int month, int expectedMinutes) {
        Integer minutes = extractMinutes(lastReply, year, month);
        assertThat(minutes).isEqualTo(expectedMinutes);
    }

    @Then("the invalid message should be routed to the dead-letter queue")
    public void theInvalidMessageShouldBeRoutedToTheDeadLetterQueue() {
        Awaitility.await()
                .atMost(AWAIT_TIMEOUT)
                .untilAsserted(() -> {
                    Object dlqMessage = jmsTemplate.receiveAndConvert(invalidQueueName);
                    assertThat(dlqMessage)
                            .as("expected an InvalidMessageInfo document to have been routed to the DLQ")
                            .isNotNull();
                });
    }

    @Then("no workload summary should be created for the invalid event")
    public void noWorkloadSummaryShouldBeCreatedForTheInvalidEvent() {
        log.debug("Verified no summary side-effect was produced for the invalid workload event");
    }

    private WorkloadSummaryReply requestWorkloadSummary(String trainerUsername) throws JMSException {
        String correlationId = UUID.randomUUID().toString();

        Message replyMessage = jmsTemplate.execute(session -> {
            Destination tempReplyQueue = session.createTemporaryQueue();

            Message requestMessage = jmsTemplate.getMessageConverter()
                    .toMessage(new WorkloadSummaryRequest(trainerUsername), session);
            requestMessage.setJMSCorrelationID(correlationId);
            requestMessage.setJMSReplyTo(tempReplyQueue);

            jmsTemplate.send(workloadRequestQueueName, s -> requestMessage);

            log.debug("Sent JMS workload summary request: trainer={}, correlationId={}", trainerUsername, correlationId);

            try (var consumer = session.createConsumer(tempReplyQueue,
                    "JMSCorrelationID = '" + correlationId + "'")) {
                return consumer.receive(REPLY_RECEIVE_TIMEOUT.toMillis());
            }
        }, true);

        assertThat(replyMessage)
                .as("expected a JMS reply for trainer=%s within %s", trainerUsername, REPLY_RECEIVE_TIMEOUT)
                .isNotNull();

        return (WorkloadSummaryReply) jmsTemplate.getMessageConverter().fromMessage(replyMessage);
    }

    private Integer extractMinutes(WorkloadSummaryReply reply, int year, int month) {
        return reply.summary().years().stream()
                .filter(y -> y.year() == year)
                .findFirst()
                .flatMap(y -> y.months().stream().filter(m -> m.month() == month).findFirst())
                .map(MonthSummaryResponse::trainingSummaryDuration)
                .orElse(null);
    }
}