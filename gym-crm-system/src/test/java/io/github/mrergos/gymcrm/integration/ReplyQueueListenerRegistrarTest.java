package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.config.JmsQueueProperties;
import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryReply;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReplyQueueListenerRegistrar tests")
class ReplyQueueListenerRegistrarTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private MessageConverter jacksonJmsMessageConverter;

    @Mock
    private InstanceReplyQueueNameProvider replyQueueNameProvider;

    @Mock
    private PendingReplyRegistry pendingReplyRegistry;

    private JmsQueueProperties queueProperties;

    private ReplyQueueListenerRegistrar registrar;

    private void initRegistrar() {
        queueProperties = new JmsQueueProperties();
        JmsQueueProperties.Queues queues = new JmsQueueProperties.Queues();
        queues.setWorkloadEvents("workload.events");
        queues.setWorkloadEventsInvalid("workload.events.invalid.dlq");
        queues.setWorkloadRequest("workload.summary.request");
        queues.setWorkloadReplyPrefix("workload.summary.reply");
        queueProperties.setQueues(queues);

        registrar = new ReplyQueueListenerRegistrar(connectionFactory, jacksonJmsMessageConverter,
                replyQueueNameProvider, pendingReplyRegistry, queueProperties);
    }

    private SimpleMessageListenerContainer getContainerField() throws Exception {
        Field field = ReplyQueueListenerRegistrar.class.getDeclaredField("container");
        field.setAccessible(true);
        return (SimpleMessageListenerContainer) field.get(registrar);
    }

    private void invokeOnSummaryReply(Message rawMessage) throws Exception {
        Method method = ReplyQueueListenerRegistrar.class
                .getDeclaredMethod("onSummaryReply", Message.class);
        method.setAccessible(true);
        try {
            method.invoke(registrar, rawMessage);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }

    @Test
    @DisplayName("onSummaryReply: delegates completion to the pending reply registry using the message correlation id")
    void onSummaryReply_shouldDelegateToRegistryComplete() throws Exception {
        //given
        initRegistrar();
        Message message = mock(Message.class);
        WorkloadSummaryReply reply = WorkloadSummaryReply.of(
                new TrainerWorkloadSummaryResponse("Jane.Smith", "Jane", "Smith", true, List.of()));

        when(jacksonJmsMessageConverter.fromMessage(message)).thenReturn(reply);
        when(message.getJMSCorrelationID()).thenReturn("corr-7");

        //when
        invokeOnSummaryReply(message);

        //then
        verify(pendingReplyRegistry).complete("corr-7", reply);
    }

    @Test
    @DisplayName("onSummaryReply: still delegates to registry even when correlation id is null (registry decides how to handle it)")
    void onSummaryReply_nullCorrelationId_shouldStillDelegateToRegistry() throws Exception {
        //given
        initRegistrar();
        Message message = mock(Message.class);
        WorkloadSummaryReply reply = WorkloadSummaryReply.notFound("not found");

        when(jacksonJmsMessageConverter.fromMessage(message)).thenReturn(reply);
        when(message.getJMSCorrelationID()).thenReturn(null);

        //when
        invokeOnSummaryReply(message);

        //then
        verify(pendingReplyRegistry).complete(null, reply);
    }

    @Test
    @DisplayName("onSummaryReply: swallows JMSException thrown while reading the message and never calls the registry")
    void onSummaryReply_messageConverterThrowsJmsException_shouldNotThrowAndSkipRegistry() throws Exception {
        //given
        initRegistrar();
        Message message = mock(Message.class);

        when(jacksonJmsMessageConverter.fromMessage(message))
                .thenThrow(new org.springframework.jms.support.converter.MessageConversionException("bad payload"));

        //when
        //then
        assertDoesNotThrow(() -> invokeOnSummaryReply(message));
        verify(pendingReplyRegistry, never()).complete(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("onSummaryReply: swallows exception thrown while reading JMSCorrelationID and never calls the registry")
    void onSummaryReply_correlationIdLookupThrowsJmsException_shouldNotThrow() throws Exception {
        //given
        initRegistrar();
        Message message = mock(Message.class);
        WorkloadSummaryReply reply = WorkloadSummaryReply.of(
                new TrainerWorkloadSummaryResponse("Jane.Smith", "Jane", "Smith", true, List.of()));

        when(jacksonJmsMessageConverter.fromMessage(message)).thenReturn(reply);
        when(message.getJMSCorrelationID()).thenThrow(new JMSException("cannot read correlation id"));

        //when
        //then
        assertDoesNotThrow(() -> invokeOnSummaryReply(message));
        verify(pendingReplyRegistry, never()).complete(eq("corr-7"), eq(reply));
    }
}