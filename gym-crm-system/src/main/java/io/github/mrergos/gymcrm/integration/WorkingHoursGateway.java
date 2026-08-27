package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.config.JmsQueueProperties;
import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.exception.ServiceUnavailableException;
import io.github.mrergos.gymcrm.integration.dto.ActionType;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryReply;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class WorkingHoursGateway {

    private static final Logger log = LoggerFactory.getLogger(WorkingHoursGateway.class);
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    private final JmsTemplate jmsTemplate;
    private final JmsQueueProperties queueProperties;
    private final PendingReplyRegistry pendingReplyRegistry;
    private final InstanceReplyQueueNameProvider replyQueueNameProvider;

    public WorkingHoursGateway(JmsTemplate jmsTemplate, JmsQueueProperties queueProperties,
                               PendingReplyRegistry pendingReplyRegistry,
                               InstanceReplyQueueNameProvider replyQueueNameProvider) {
        this.jmsTemplate = jmsTemplate;
        this.queueProperties = queueProperties;
        this.pendingReplyRegistry = pendingReplyRegistry;
        this.replyQueueNameProvider = replyQueueNameProvider;
    }

    public void notify(Training training, ActionType actionType) {
        String transactionId = currentOrNewTransactionId();
        try {
            TrainerWorkloadRequest request = toRequest(training, actionType);

            log.info("Publishing workload event to working-hours-service: trainer={}, action={}, txId={}",
                    request.trainerUsername(), actionType, transactionId);

            jmsTemplate.convertAndSend(queueProperties.getQueues().getWorkloadEvents(), request, message -> {
                message.setStringProperty("transactionId", transactionId);
                return message;
            });

            log.info("Workload event published: trainer={}, action={}, txId={}",
                    request.trainerUsername(), actionType, transactionId);
        } catch (Exception e) {
            log.error("Failed to publish workload event, skipping: " +
                    "action={}, txId={}, error={}", actionType, transactionId, e.toString());
        }
    }

    public TrainerWorkloadSummaryResponse getWorkloadSummary(String username) {
        String transactionId = currentOrNewTransactionId();
        PendingReplyRegistry.Registration registration = pendingReplyRegistry.register();

        log.info("Requesting workload summary from working-hours-service: trainer={}, correlationId={}, txId={}",
                username, registration.correlationId(), transactionId);

        try {
            publishSummaryRequest(username, registration.correlationId(), transactionId);

            WorkloadSummaryReply reply = registration.future()
                    .get(queueProperties.getReplyTimeoutMs(), TimeUnit.MILLISECONDS);
            return unwrap(username, reply);
        } catch (ExecutionException e) {
            throw mapWorkloadSummaryFailure(username, transactionId, e.getCause() != null ? e.getCause() : e);
        } catch (TimeoutException e) {
            log.warn("Timed out waiting for working-hours-service workload summary reply: trainer={}, txId={}",
                    username, transactionId);
            throw new ServiceUnavailableException(
                    "working-hours-service did not respond in time, please try again later");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException(
                    "Interrupted while waiting for working-hours-service response, trainer=" + username, e);
        } catch (JmsException e) {
            throw mapWorkloadSummaryFailure(username, transactionId, e);
        } finally {
            pendingReplyRegistry.remove(registration.correlationId());
        }
    }

    private void publishSummaryRequest(String username, String correlationId, String transactionId) throws JmsException {
        String replyQueueName = replyQueueNameProvider.getReplyQueueName();

        jmsTemplate.send(queueProperties.getQueues().getWorkloadRequest(), session -> {
            Message message = jmsTemplate.getMessageConverter()
                    .toMessage(new WorkloadSummaryRequest(username), session);
            message.setJMSCorrelationID(correlationId);
            message.setJMSReplyTo(session.createQueue(replyQueueName));
            message.setStringProperty("transactionId", transactionId);
            return message;
        });
    }

    private TrainerWorkloadSummaryResponse unwrap(String username, WorkloadSummaryReply reply) {
        if (!reply.found()) {
            log.warn("No workload data found for trainer in working-hours-service: trainer={}", username);
            throw new EntityNotFoundException("No workload data found for trainer: " + username);
        }
        log.info("Workload summary received from working-hours-service: trainer={}", username);
        return reply.summary();
    }

    private ServiceUnavailableException mapWorkloadSummaryFailure(String username, String transactionId, Throwable throwable) {
        log.error("Failed to fetch workload summary from working-hours-service: trainer={}, txId={}, error={}",
                username, transactionId, throwable.toString());
        return new ServiceUnavailableException(
                "working-hours-service is currently unavailable, please try again later", throwable);
    }

    private TrainerWorkloadRequest toRequest(Training training, ActionType actionType) {
        var trainer = training.getTrainer();
        return new TrainerWorkloadRequest(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
    }

    private String currentOrNewTransactionId() {
        String existing = MDC.get(TRANSACTION_ID_MDC_KEY);
        return (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
    }
}