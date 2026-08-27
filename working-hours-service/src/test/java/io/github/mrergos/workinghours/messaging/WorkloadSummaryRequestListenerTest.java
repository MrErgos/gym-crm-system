package io.github.mrergos.workinghours.messaging;

import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.exception.EntityNotFoundException;
import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryReply;
import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryRequest;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadSummaryRequestListener tests")
class WorkloadSummaryRequestListenerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @Mock
    private JmsTemplate jmsTemplate;

    private WorkloadSummaryRequestListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkloadSummaryRequestListener(trainerWorkloadService, jmsTemplate);
    }

    private Message buildRawMessage(String correlationId, String replyQueueName) throws JMSException {
        return buildRawMessage(correlationId, replyQueueName, "tx-default");
    }

    private Message buildRawMessage(String correlationId, String replyQueueName, String transactionId) throws JMSException {
        Message rawMessage = mock(Message.class);
        when(rawMessage.getJMSCorrelationID()).thenReturn(correlationId);
        when(rawMessage.getStringProperty("transactionId")).thenReturn(transactionId);
        if (replyQueueName != null) {
            Queue replyQueue = mock(Queue.class);
            when(replyQueue.getQueueName()).thenReturn(replyQueueName);
            when(rawMessage.getJMSReplyTo()).thenReturn(replyQueue);
        } else {
            when(rawMessage.getJMSReplyTo()).thenReturn(null);
        }
        return rawMessage;
    }

    @Test
    @DisplayName("onSummaryRequest: sends a found reply with the same correlation id when trainer exists")
    void onSummaryRequest_trainerFound_shouldSendReplyWithSummaryAndSameCorrelationId() throws JMSException {
        //given
        String username = "Jane.Smith";
        String correlationId = "corr-1";
        String replyQueueName = "workload.summary.reply";
        WorkloadSummaryRequest request = new WorkloadSummaryRequest(username);
        Message rawMessage = buildRawMessage(correlationId, replyQueueName, "tx-1");

        TrainerWorkloadSummaryResponse summary =
                new TrainerWorkloadSummaryResponse(username, "Jane", "Smith", true, List.of());
        when(trainerWorkloadService.getSummary(username)).thenReturn(summary);

        MessageConverter converter = mock(MessageConverter.class);
        Message replyMessage = mock(Message.class);
        Session session = mock(Session.class);
        when(jmsTemplate.getMessageConverter()).thenReturn(converter);
        when(converter.toMessage(any(WorkloadSummaryReply.class), eq(session))).thenReturn(replyMessage);

        //when
        listener.onSummaryRequest(request, rawMessage);

        //then
        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(replyQueueName), creatorCaptor.capture());

        Message created = creatorCaptor.getValue().createMessage(session);
        assertEquals(replyMessage, created);
        verify(replyMessage).setJMSCorrelationID(correlationId);
        verify(replyMessage).setStringProperty("transactionId", "tx-1");

        ArgumentCaptor<WorkloadSummaryReply> replyCaptor = ArgumentCaptor.forClass(WorkloadSummaryReply.class);
        verify(converter).toMessage(replyCaptor.capture(), eq(session));
        assertTrue(replyCaptor.getValue().found());
        assertEquals(summary, replyCaptor.getValue().summary());
    }



    @Test
    @DisplayName("onSummaryRequest: sends a not-found reply when the trainer has no workload data")
    void onSummaryRequest_trainerNotFound_shouldSendNotFoundReply() throws JMSException {
        //given
        String username = "Unknown.User";
        String correlationId = "corr-2";
        String replyQueueName = "workload.summary.reply";
        WorkloadSummaryRequest request = new WorkloadSummaryRequest(username);
        Message rawMessage = buildRawMessage(correlationId, replyQueueName, "tx-2");

        when(trainerWorkloadService.getSummary(username))
                .thenThrow(new EntityNotFoundException("Trainer not found: " + username));

        MessageConverter converter = mock(MessageConverter.class);
        Message replyMessage = mock(Message.class);
        Session session = mock(Session.class);
        when(jmsTemplate.getMessageConverter()).thenReturn(converter);
        when(converter.toMessage(any(WorkloadSummaryReply.class), eq(session))).thenReturn(replyMessage);

        doAnswer(invocation -> {
            MessageCreator messageCreator = invocation.getArgument(1);
            messageCreator.createMessage(session);
            return null;
        }).when(jmsTemplate).send(eq(replyQueueName), any());

        //when
        listener.onSummaryRequest(request, rawMessage);

        //then
        ArgumentCaptor<WorkloadSummaryReply> replyCaptor = ArgumentCaptor.forClass(WorkloadSummaryReply.class);
        verify(converter).toMessage(replyCaptor.capture(), eq(session));
        WorkloadSummaryReply reply = replyCaptor.getValue();

        assertFalse(reply.found());
        assertEquals("Trainer not found: " + username, reply.errorMessage());
    }

    @Test
    @DisplayName("onSummaryRequest: discards the request silently when JMSReplyTo is missing")
    void onSummaryRequest_missingReplyTo_shouldDiscardSilently() throws JMSException {
        //given
        WorkloadSummaryRequest request = new WorkloadSummaryRequest("Jane.Smith");
        Message rawMessage = buildRawMessage("corr-3", null, "tx-3");

        //when
        listener.onSummaryRequest(request, rawMessage);

        //then
        verify(jmsTemplate, never()).send(anyString(), any(MessageCreator.class));
        verify(trainerWorkloadService, never()).getSummary(anyString());
    }

    @Test
    @DisplayName("onSummaryRequest: discards the request silently when JMSCorrelationID is missing")
    void onSummaryRequest_missingCorrelationId_shouldDiscardSilently() throws JMSException {
        //given
        WorkloadSummaryRequest request = new WorkloadSummaryRequest("Jane.Smith");
        Message rawMessage = buildRawMessage(null, "workload.summary.reply", "tx-4");

        //when
        listener.onSummaryRequest(request, rawMessage);

        //then
        verify(jmsTemplate, never()).send(anyString(), any(MessageCreator.class));
        verify(trainerWorkloadService, never()).getSummary(anyString());
    }

    @Test
    @DisplayName("onSummaryRequest: discards the request silently when request is missing")
    void onSummaryRequest_missingRequest_shouldDiscardSilently() throws JMSException {
        //given
        String replyQueueName = "workload.summary.reply";
        Message rawMessage = buildRawMessage(null, replyQueueName, "tx-5");

        //when
        listener.onSummaryRequest(null, rawMessage);

        //then
        verify(jmsTemplate, never()).send(anyString(), any(MessageCreator.class));
        verify(trainerWorkloadService, never()).getSummary(anyString());
    }

    @Test
    @DisplayName("onSummaryRequest: falls back to a generated transactionId when the JMS property is missing")
    void onSummaryRequest_missingTransactionIdProperty_shouldStillProcessAndPropagateGeneratedId() throws JMSException {
        //given
        String username = "Jane.Smith";
        String correlationId = "corr-6";
        String replyQueueName = "workload.summary.reply";
        WorkloadSummaryRequest request = new WorkloadSummaryRequest(username);
        Message rawMessage = buildRawMessage(correlationId, replyQueueName, null);

        TrainerWorkloadSummaryResponse summary =
                new TrainerWorkloadSummaryResponse(username, "Jane", "Smith", true, List.of());
        when(trainerWorkloadService.getSummary(username)).thenReturn(summary);

        MessageConverter converter = mock(MessageConverter.class);
        Message replyMessage = mock(Message.class);
        Session session = mock(Session.class);
        when(jmsTemplate.getMessageConverter()).thenReturn(converter);
        when(converter.toMessage(any(WorkloadSummaryReply.class), eq(session))).thenReturn(replyMessage);

        //when
        listener.onSummaryRequest(request, rawMessage);

        //then
        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(replyQueueName), creatorCaptor.capture());

        Message created = creatorCaptor.getValue().createMessage(session);
        assertEquals(replyMessage, created);
        verify(replyMessage).setJMSCorrelationID(correlationId);
        verify(replyMessage).setStringProperty(eq("transactionId"), anyString());
    }
}
