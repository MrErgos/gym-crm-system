package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.config.JmsQueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Component
public class InstanceReplyQueueNameProvider {

    private static final Logger log = LoggerFactory.getLogger(InstanceReplyQueueNameProvider.class);

    private final String replyQueueName;

    public InstanceReplyQueueNameProvider(JmsQueueProperties queueProperties) {
        String prefix = queueProperties.getQueues().getWorkloadReplyPrefix();
        String hostname = resolveHostname();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        this.replyQueueName = prefix + "." + hostname + "-" + uniqueSuffix;
        log.info("Resolved instance-unique workload reply queue name: {}", replyQueueName);
    }

    public String getReplyQueueName() {
        return replyQueueName;
    }

    private String resolveHostname() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return sanitize(hostname);
        } catch (UnknownHostException e) {
            log.warn("Could not resolve hostname for reply queue naming, falling back to random id", e);
            return "instance";
        }
    }

    private String sanitize(String hostname) {
        return hostname.replaceAll("[^a-zA-Z0-9-]", "-");
    }
}