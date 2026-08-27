package io.github.mrergos.workinghours.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.mrergos.workinghours.config.JmsQueueProperties;
import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.entity.ActionType;
import io.github.mrergos.workinghours.messaging.dto.InvalidMessageInfo;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadListener tests")
class TrainerWorkloadListenerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @Mock
    private WorkloadMessageValidator validator;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private JmsQueueProperties queueProperties;

    private TrainerWorkloadListener listener;

    private TrainerWorkloadRequest buildValidRequest() {
        return new TrainerWorkloadRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2026, 8, 1), 60, ActionType.ADD);
    }

    private Message buildRawMessage(String transactionId) throws JMSException {
        Message rawMessage = mock(Message.class);
        when(rawMessage.getStringProperty("transactionId")).thenReturn(transactionId);
        return rawMessage;
    }

    @BeforeEach
    void setUp() {
        queueProperties = new JmsQueueProperties();
        JmsQueueProperties.Queues queues = new JmsQueueProperties.Queues();
        queues.setWorkloadEvents("workload.events");
        queues.setWorkloadEventsInvalid("workload.events.invalid.dlq");
        queues.setWorkloadRequest("workload.summary.request");
        queueProperties.setQueues(queues);

        listener = new TrainerWorkloadListener(trainerWorkloadService, validator, jmsTemplate, queueProperties, objectMapper);
    }

    @Test
    @DisplayName("onWorkloadEvent: delegates to service and does not touch the DLQ when the message is valid")
    void onWorkloadEvent_validRequest_shouldDelegateToService() throws JMSException {
        //given
        TrainerWorkloadRequest request = buildValidRequest();
        Message rawMessage = buildRawMessage("tx-1");
        when(validator.validate(request)).thenReturn(List.of());

        //when
        listener.onWorkloadEvent(request, rawMessage);

        //then
        verify(trainerWorkloadService).applyWorkload(request);
        verify(jmsTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("onWorkloadEvent: falls back to a generated transactionId when the JMS property is missing")
    void onWorkloadEvent_missingTransactionIdProperty_shouldStillDelegateToService() throws JMSException {
        //given
        TrainerWorkloadRequest request = buildValidRequest();
        Message rawMessage = buildRawMessage(null);
        when(validator.validate(request)).thenReturn(List.of());

        //when
        listener.onWorkloadEvent(request, rawMessage);

        //then
        verify(trainerWorkloadService).applyWorkload(request);
    }

    @Test
    @DisplayName("onWorkloadEvent: routes invalid message to the DLQ and skips business processing")
    void onWorkloadEvent_missingRequiredFields_shouldRouteToInvalidQueueAndSkipService() throws JsonProcessingException, JMSException {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                null, "Jane", "Smith", true, LocalDate.of(2026, 8, 1), 60, ActionType.ADD);
        Message rawMessage = buildRawMessage("tx-2");
        List<String> validationErrors = List.of("trainerUsername is required");
        when(validator.validate(request)).thenReturn(validationErrors);
        when(objectMapper.writeValueAsString(request)).thenReturn("{\"trainerUsername\":null}");

        //when
        listener.onWorkloadEvent(request, rawMessage);

        //then
        verify(trainerWorkloadService, never()).applyWorkload(any());

        ArgumentCaptor<InvalidMessageInfo> captor = ArgumentCaptor.forClass(InvalidMessageInfo.class);
        verify(jmsTemplate).convertAndSend(eq("workload.events.invalid.dlq"), captor.capture());

        InvalidMessageInfo dlqPayload = captor.getValue();
        assertEquals("workload.events", dlqPayload.sourceQueue());
        assertEquals(validationErrors, dlqPayload.validationErrors());
        assertEquals("{\"trainerUsername\":null}", dlqPayload.originalPayload());
    }

    @Test
    @DisplayName("onWorkloadEvent: falls back to toString() for the DLQ payload when JSON serialization fails")
    void onWorkloadEvent_serializationFailsForDlqPayload_shouldFallBackToToString() throws JsonProcessingException, JMSException {
        //given
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                null, null, null, null, null, null, null);
        Message rawMessage = buildRawMessage("tx-3");
        List<String> validationErrors = List.of("trainerUsername is required", "trainingDate is required");
        when(validator.validate(request)).thenReturn(validationErrors);
        when(objectMapper.writeValueAsString(request)).thenThrow(new JsonProcessingException("boom") {});

        //when
        listener.onWorkloadEvent(request, rawMessage);

        //then
        ArgumentCaptor<InvalidMessageInfo> captor = ArgumentCaptor.forClass(InvalidMessageInfo.class);
        verify(jmsTemplate).convertAndSend(eq("workload.events.invalid.dlq"), captor.capture());
        InvalidMessageInfo dlqPayload = captor.getValue();
        assertTrue(dlqPayload.originalPayload().contains("TrainerWorkloadRequest"));
        verify(trainerWorkloadService, never()).applyWorkload(any());
    }

    @Test
    @DisplayName("onWorkloadEvent: null request is treated as invalid and routed to the DLQ")
    void onWorkloadEvent_nullRequest_shouldRouteToInvalidQueue() throws JMSException {
        //given
        Message rawMessage = buildRawMessage("tx-4");
        when(validator.validate(null)).thenReturn(List.of("Message payload is null or could not be parsed as TrainerWorkloadRequest"));

        //when
        listener.onWorkloadEvent(null, rawMessage);

        //then
        verify(trainerWorkloadService, never()).applyWorkload(any());
        verify(jmsTemplate).convertAndSend(eq("workload.events.invalid.dlq"), any(InvalidMessageInfo.class));
    }
}
