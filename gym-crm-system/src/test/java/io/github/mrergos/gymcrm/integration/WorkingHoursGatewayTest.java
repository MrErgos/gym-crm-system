package io.github.mrergos.gymcrm.integration;


import io.github.mrergos.gymcrm.config.JmsQueueProperties;
import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.exception.ServiceUnavailableException;
import io.github.mrergos.gymcrm.integration.dto.ActionType;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryReply;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.converter.MessageConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkingHoursGateway tests")
class WorkingHoursGatewayTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private PendingReplyRegistry pendingReplyRegistry;

    private JmsQueueProperties queueProperties;

    private WorkingHoursGateway gateway;

    private void initGateway(long replyTimeoutMs) {
        queueProperties = new JmsQueueProperties();
        JmsQueueProperties.Queues queues = new JmsQueueProperties.Queues();
        queues.setWorkloadEvents("workload.events");
        queues.setWorkloadEventsInvalid("workload.events.invalid.dlq");
        queues.setWorkloadRequest("workload.summary.request");
        queues.setWorkloadReplyPrefix("workload.summary.reply");
        queueProperties.setQueues(queues);
        queueProperties.setReplyTimeoutMs(replyTimeoutMs);

        gateway = new WorkingHoursGateway(jmsTemplate, queueProperties, pendingReplyRegistry);
    }

    private Trainer buildTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Jane.Smith");
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setActive(true);
        trainer.setSpecialization(new TrainingType("yoga"));
        return trainer;
    }

    private Training buildTraining(Trainer trainer) {
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(new TrainingType("yoga"));
        training.setTrainingDate(LocalDate.of(2026, 8, 1));
        training.setTrainingDuration(60);
        return training;
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("notify: publishes mapped workload request to the workload events queue")
    void notify_valid_shouldPublishMappedRequest() {
        //given
        initGateway(5000L);
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        //when
        gateway.notify(training, ActionType.ADD);

        //then
        ArgumentCaptor<TrainerWorkloadRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq("workload.events"), captor.capture(), any());

        TrainerWorkloadRequest request = captor.getValue();
        assertEquals("Jane.Smith", request.trainerUsername());
        assertEquals("Jane", request.trainerFirstName());
        assertEquals("Smith", request.trainerLastName());
        assertEquals(Boolean.TRUE, request.isActive());
        assertEquals(LocalDate.of(2026, 8, 1), request.trainingDate());
        assertEquals(60, request.trainingDuration());
        assertEquals(ActionType.ADD, request.actionType());
    }

    @Test
    @DisplayName("notify: reuses existing transaction id from MDC as a message property")
    void notify_withExistingMdcTransactionId_shouldReuseIt() throws JMSException {
        //given
        initGateway(5000L);
        MDC.put("transactionId", "existing-tx-id");
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        //when
        gateway.notify(training, ActionType.ADD);

        //then
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq("workload.events"), any(), postProcessorCaptor.capture());

        Message message = mock(Message.class);
        postProcessorCaptor.getValue().postProcessMessage(message);
        verify(message).setStringProperty("transactionId", "existing-tx-id");
    }

    @Test
    @DisplayName("notify: generates a new transaction id when MDC is empty")
    void notify_withoutMdcTransactionId_shouldGenerateNewOne() throws JMSException {
        //given
        initGateway(5000L);
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        //when
        gateway.notify(training, ActionType.ADD);

        //then
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq("workload.events"), any(), postProcessorCaptor.capture());

        Message message = mock(Message.class);
        postProcessorCaptor.getValue().postProcessMessage(message);

        ArgumentCaptor<String> txIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(message).setStringProperty(eq("transactionId"), txIdCaptor.capture());
        assertDoesNotThrow(() -> UUID.fromString(txIdCaptor.getValue()));
    }

    @Test
    @DisplayName("notify: catches and swallows exception thrown by jmsTemplate")
    void notify_jmsTemplateThrows_shouldNotThrow() {
        //given
        initGateway(5000L);
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        doThrow(new RuntimeException("broker unreachable"))
                .when(jmsTemplate).convertAndSend(eq("workload.events"), any(), any());

        //when
        //then
        assertDoesNotThrow(() -> gateway.notify(training, ActionType.ADD));
    }

    @Test
    @DisplayName("notify: maps DELETE action type into the published request")
    void notify_delete_shouldPublishRequestWithDeleteActionType() {
        //given
        initGateway(5000L);
        Trainer trainer = buildTrainer();
        Training training = buildTraining(trainer);

        //when
        gateway.notify(training, ActionType.DELETE);

        //then
        ArgumentCaptor<TrainerWorkloadRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq("workload.events"), captor.capture(), any());
        assertEquals(ActionType.DELETE, captor.getValue().actionType());
    }

    @Test
    @DisplayName("getWorkloadSummary: returns summary when reply future completes with found=true")
    void getWorkloadSummary_replyFound_shouldReturnSummary() throws JMSException {
        //given
        initGateway(5000L);
        String username = "Jane.Smith";
        TrainerWorkloadSummaryResponse expected = new TrainerWorkloadSummaryResponse(
                username, "Jane", "Smith", true, List.of());

        String correlationId = "corr-1";
        CompletableFuture<WorkloadSummaryReply> future =
                CompletableFuture.completedFuture(WorkloadSummaryReply.of(expected));
        when(pendingReplyRegistry.register())
                .thenReturn(new PendingReplyRegistry.Registration(correlationId, future));

        Session session = mock(Session.class);
        Queue replyQueue = mock(Queue.class);
        when(session.createQueue("workload.summary.reply")).thenReturn(replyQueue);

        MessageConverter converter = mock(MessageConverter.class);
        Message message = mock(Message.class);
        when(jmsTemplate.getMessageConverter()).thenReturn(converter);
        when(converter.toMessage(any(WorkloadSummaryRequest.class), eq(session))).thenReturn(message);

        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        //when
        TrainerWorkloadSummaryResponse result = gateway.getWorkloadSummary(username);

        //then
        verify(jmsTemplate).send(eq("workload.summary.request"), creatorCaptor.capture());
        Message created = creatorCaptor.getValue().createMessage(session);
        assertEquals(message, created);
        verify(message).setJMSCorrelationID(correlationId);
        verify(message).setJMSReplyTo(replyQueue);
        verify(message).setStringProperty(eq("transactionId"), anyString());

        assertEquals(expected, result);
        verify(pendingReplyRegistry).remove(correlationId);
    }

    @Test
    @DisplayName("getWorkloadSummary: throws EntityNotFoundException when reply reports found=false")
    void getWorkloadSummary_replyNotFound_shouldThrowEntityNotFoundException() {
        //given
        initGateway(5000L);
        String username = "Unknown.User";
        CompletableFuture<WorkloadSummaryReply> future =
                CompletableFuture.completedFuture(WorkloadSummaryReply.notFound("No workload data found"));
        when(pendingReplyRegistry.register())
                .thenReturn(new PendingReplyRegistry.Registration("corr-2", future));
        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> gateway.getWorkloadSummary(username));
        verify(pendingReplyRegistry).remove("corr-2");
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException when reply future times out")
    void getWorkloadSummary_timeout_shouldThrowServiceUnavailableException() {
        //given
        initGateway(50L);
        String username = "Jane.Smith";
        CompletableFuture<WorkloadSummaryReply> neverCompletes = new CompletableFuture<>();
        when(pendingReplyRegistry.register())
                .thenReturn(new PendingReplyRegistry.Registration("corr-3", neverCompletes));
        //when
        //then
        assertThrows(ServiceUnavailableException.class, () -> gateway.getWorkloadSummary(username));
        verify(pendingReplyRegistry).remove("corr-3");
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException for unknown reply failure")
    void getWorkloadSummary_unknownFailure_shouldThrowServiceUnavailableException() {
        //given
        initGateway(5000L);
        String username = "Jane.Smith";
        RuntimeException cause = new RuntimeException("unexpected");
        CompletableFuture<WorkloadSummaryReply> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(cause);
        when(pendingReplyRegistry.register())
                .thenReturn(new PendingReplyRegistry.Registration("corr-4", failedFuture));
        //when
        //then
        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class,
                () -> gateway.getWorkloadSummary(username));
        assertEquals(cause, ex.getCause());
        verify(pendingReplyRegistry).remove("corr-4");
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException and preserves interrupt status")
    void getWorkloadSummary_interrupted_shouldThrowServiceUnavailableExceptionAndPreserveInterruptFlag() {
        //given
        initGateway(5000L);
        String username = "Jane.Smith";

        CompletableFuture<WorkloadSummaryReply> interruptingFuture = mock(CompletableFuture.class);
        when(pendingReplyRegistry.register())
                .thenReturn(new PendingReplyRegistry.Registration("corr-5", interruptingFuture));
        try {
            when(interruptingFuture.get(5000L, java.util.concurrent.TimeUnit.MILLISECONDS))
                    .thenThrow(new InterruptedException("interrupted"));
        } catch (InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new RuntimeException(e);
        }

        //when
        //then
        try {
            assertThrows(ServiceUnavailableException.class, () -> gateway.getWorkloadSummary(username));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        verify(pendingReplyRegistry).remove("corr-5");
    }

    @Test
    @DisplayName("getWorkloadSummary: throws ServiceUnavailableException and cleans up registration when send fails")
    void getWorkloadSummary_sendThrowsJmsException_shouldThrowServiceUnavailableExceptionAndRemoveRegistration() {
        //given
        initGateway(5000L);
        String username = "Jane.Smith";
        CompletableFuture<WorkloadSummaryReply> future = new CompletableFuture<>();
        when(pendingReplyRegistry.register())
                .thenReturn(new PendingReplyRegistry.Registration("corr-6", future));

        doThrow(new JmsException("broker down") {})
                .when(jmsTemplate).send(eq("workload.summary.request"), any(MessageCreator.class));

        //when
        //then
        assertThrows(ServiceUnavailableException.class, () -> gateway.getWorkloadSummary(username));
        verify(pendingReplyRegistry).remove("corr-6");
    }

    @Test
    @DisplayName("onSummaryReply: delegates completion to the pending reply registry using the message correlation id")
    void onSummaryReply_shouldDelegateToRegistryComplete() throws JMSException {
        //given
        initGateway(5000L);
        Message message = mock(Message.class);
        when(message.getJMSCorrelationID()).thenReturn("corr-7");
        WorkloadSummaryReply reply = WorkloadSummaryReply.of(
                new TrainerWorkloadSummaryResponse("Jane.Smith", "Jane", "Smith", true, List.of()));

        //when
        gateway.onSummaryReply(reply, message);

        //then
        verify(pendingReplyRegistry).complete("corr-7", reply);
    }

    @Test
    @DisplayName("onSummaryReply: still delegates to registry even when correlation id is null (registry decides how to handle it)")
    void onSummaryReply_nullCorrelationId_shouldStillDelegateToRegistry() throws JMSException {
        //given
        initGateway(5000L);
        Message message = mock(Message.class);
        when(message.getJMSCorrelationID()).thenReturn(null);
        WorkloadSummaryReply reply = WorkloadSummaryReply.notFound("not found");

        //when
        gateway.onSummaryReply(reply, message);

        //then
        verify(pendingReplyRegistry).complete(null, reply);
        verify(jmsTemplate, never()).convertAndSend(anyString(), any(), any(MessagePostProcessor.class));
    }
}