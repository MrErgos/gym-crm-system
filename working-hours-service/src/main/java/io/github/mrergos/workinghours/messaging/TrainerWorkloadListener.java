package io.github.mrergos.workinghours.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrergos.workinghours.config.JmsQueueProperties;
import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.messaging.dto.InvalidMessageInfo;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TrainerWorkloadListener {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadListener.class);
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";
    private static final String TRANSACTION_ID_JMS_PROPERTY = "transactionId";

    private final TrainerWorkloadService trainerWorkloadService;
    private final WorkloadMessageValidator validator;
    private final JmsTemplate jmsTemplate;
    private final JmsQueueProperties queueProperties;
    private final ObjectMapper objectMapper;

    public TrainerWorkloadListener(TrainerWorkloadService trainerWorkloadService,
                                   WorkloadMessageValidator validator,
                                   JmsTemplate jmsTemplate,
                                   JmsQueueProperties queueProperties,
                                   ObjectMapper objectMapper) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.validator = validator;
        this.jmsTemplate = jmsTemplate;
        this.queueProperties = queueProperties;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${jms.queues.workload-events}",
            containerFactory = "jmsListenerContainerFactory")
    public void onWorkloadEvent(TrainerWorkloadRequest request, Message rawMessage) throws JMSException {
        String transactionId = resolveTransactionId(rawMessage);
        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        try {
            List<String> validationErrors = validator.validate(request);

            if (!validationErrors.isEmpty()) {
                log.warn("Rejecting workload event with missing required information, errors={}", validationErrors);
                sendToInvalidMessageQueue(request, validationErrors);
                return;
            }

            log.info("Received workload event: trainer={}, action={}, date={}, duration={}min",
                request.trainerUsername(), request.actionType(), request.trainingDate(), request.trainingDuration());

            trainerWorkloadService.applyWorkload(request);

            log.info("Workload event applied successfully for trainer={}", request.trainerUsername());
        } finally {
            MDC.remove(TRANSACTION_ID_MDC_KEY);
        }
    }

    private void sendToInvalidMessageQueue(TrainerWorkloadRequest request, List<String> validationErrors) {
        String rawPayload = serialize(request);
        InvalidMessageInfo invalidMessageInfo = new InvalidMessageInfo(
                rawPayload,
                queueProperties.getQueues().getWorkloadEvents(),
                validationErrors,
                Instant.now()
        );

        jmsTemplate.convertAndSend(queueProperties.getQueues().getWorkloadEventsInvalid(), invalidMessageInfo);
        log.info("Invalid workload event routed to DLQ={}", queueProperties.getQueues().getWorkloadEventsInvalid());
    }

    private String serialize(TrainerWorkloadRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.debug("Could not serialize invalid message payload for DLQ, falling back to toString()", e);
            return String.valueOf(request);
        }
    }

    private String resolveTransactionId(Message rawMessage) throws JMSException {
        String incoming = rawMessage.getStringProperty(TRANSACTION_ID_JMS_PROPERTY);
        return (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();
    }
}