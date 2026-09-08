package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.config.JmsQueueProperties;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryReply;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

@Component
public class ReplyQueueListenerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ReplyQueueListenerRegistrar.class);

    private final ConnectionFactory connectionFactory;
    private final MessageConverter jacksonJmsMessageConverter;
    private final InstanceReplyQueueNameProvider replyQueueNameProvider;
    private final PendingReplyRegistry pendingReplyRegistry;
    private final JmsQueueProperties queueProperties;

    private SimpleMessageListenerContainer container;

    public ReplyQueueListenerRegistrar(@Qualifier("jmsConnectionFactory") ConnectionFactory connectionFactory,
                                       MessageConverter jacksonJmsMessageConverter,
                                       InstanceReplyQueueNameProvider replyQueueNameProvider,
                                       PendingReplyRegistry pendingReplyRegistry,
                                       JmsQueueProperties queueProperties) {
        this.connectionFactory = connectionFactory;
        this.jacksonJmsMessageConverter = jacksonJmsMessageConverter;
        this.replyQueueNameProvider = replyQueueNameProvider;
        this.pendingReplyRegistry = pendingReplyRegistry;
        this.queueProperties = queueProperties;
    }

    @PostConstruct
    public void start() {
        String destination = replyQueueNameProvider.getReplyQueueName();

        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(destination);
        container.setSessionTransacted(true);
        container.setConcurrentConsumers(1);
        container.setMessageListener((MessageListener) this::onSummaryReply);
        container.setAutoStartup(true);
        container.afterPropertiesSet();
        container.start();

        log.info("Started dynamic JMS listener on instance-unique workload reply queue: {}", destination);
    }

    @PreDestroy
    public void stop() {
        if (container != null) {
            container.stop();
            container.destroy();
            log.info("Stopped listener on instance-unique workload reply queue: {}", replyQueueNameProvider.getReplyQueueName());
        }
    }

    private void onSummaryReply(Message rawMessage) {
        try {
            WorkloadSummaryReply reply = (WorkloadSummaryReply)
                    jacksonJmsMessageConverter.fromMessage(rawMessage);
            String correlationId = rawMessage.getJMSCorrelationID();
            pendingReplyRegistry.complete(correlationId, reply);
        } catch (JMSException | RuntimeException e) {
            log.error("Failed to process workload summary reply message on instance reply queue={}",
                    replyQueueNameProvider.getReplyQueueName(), e);
        }
    }
}