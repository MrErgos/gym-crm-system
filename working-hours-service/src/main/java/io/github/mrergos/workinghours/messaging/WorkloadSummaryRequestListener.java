package io.github.mrergos.workinghours.messaging;

import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.exception.EntityNotFoundException;
import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryReply;
import io.github.mrergos.workinghours.messaging.dto.WorkloadSummaryRequest;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;


@Component
public class WorkloadSummaryRequestListener {

    private static final Logger log = LoggerFactory.getLogger(WorkloadSummaryRequestListener.class);

    private final TrainerWorkloadService trainerWorkloadService;
    private final JmsTemplate jmsTemplate;

    public WorkloadSummaryRequestListener(TrainerWorkloadService trainerWorkloadService, JmsTemplate jmsTemplate) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "${jms.queues.workload-request}",
            containerFactory = "jmsListenerContainerFactory")
    public void onSummaryRequest(WorkloadSummaryRequest request, Message rawMessage) throws JMSException {
        String replyQueueName = resolveReplyQueueName(rawMessage);
        String correlationId = rawMessage.getJMSCorrelationID();

        if (replyQueueName == null || correlationId == null) {
            log.warn("Discarding workload summary request without JMSReplyTo/JMSCorrelationID, trainer={}",
                    request != null ? request.trainerUsername() : "unknown");
            return;
        }

        log.debug("Received workload summary request: trainer={}, correlationId={}",
                request.trainerUsername(), correlationId);

        WorkloadSummaryReply reply = buildReply(request.trainerUsername());

        jmsTemplate.send(replyQueueName, session -> {
            Message replyMessage = jmsTemplate.getMessageConverter().toMessage(reply, session);
            replyMessage.setJMSCorrelationID(correlationId);
            return replyMessage;
        });

        log.debug("Workload summary reply sent: trainer={}, correlationId={}, found={}",
                request.trainerUsername(), correlationId, reply.found());
    }

    private WorkloadSummaryReply buildReply(String trainerUsername) {
        try {
            TrainerWorkloadSummaryResponse summary = trainerWorkloadService.getSummary(trainerUsername);
            return WorkloadSummaryReply.of(summary);
        } catch (EntityNotFoundException e) {
            log.warn("No workload data found for trainer requested via messaging, trainer={}", trainerUsername);
            return WorkloadSummaryReply.notFound(e.getMessage());
        }
    }

    private String resolveReplyQueueName(Message rawMessage) throws JMSException {
        if (rawMessage.getJMSReplyTo() instanceof Queue queue) {
            return queue.getQueueName();
        }
        return null;
    }
}