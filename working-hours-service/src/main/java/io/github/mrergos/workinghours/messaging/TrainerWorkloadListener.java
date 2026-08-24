package io.github.mrergos.workinghours.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrergos.workinghours.config.JmsQueueProperties;
import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.messaging.dto.InvalidMessageInfo;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TrainerWorkloadListener {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadListener.class);

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
    public void onWorkloadEvent(TrainerWorkloadRequest request) {
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
}