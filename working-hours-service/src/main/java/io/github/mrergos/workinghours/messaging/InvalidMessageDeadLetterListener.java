package io.github.mrergos.workinghours.messaging;

import io.github.mrergos.workinghours.messaging.dto.InvalidMessageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class InvalidMessageDeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(InvalidMessageDeadLetterListener.class);

    @JmsListener(destination = "${jms.queues.workload-events-invalid}",
            containerFactory = "jmsListenerContainerFactory")
    public void onInvalidMessage(InvalidMessageInfo invalidMessageInfo) {
        log.warn("Invalid workload message received on DLQ: sourceQueue={}, receivedAt={}, errors={}",
                invalidMessageInfo.sourceQueue(), invalidMessageInfo.receivedAt(), invalidMessageInfo.validationErrors());
        log.debug("Invalid workload message raw payload: {}", invalidMessageInfo.originalPayload());
    }
}