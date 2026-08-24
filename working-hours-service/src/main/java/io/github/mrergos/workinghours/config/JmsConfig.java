package io.github.mrergos.workinghours.config;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter(JsonMapper jsonMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeMappings = new HashMap<>();
        typeMappings.put("TrainerWorkloadRequest", TrainerWorkloadRequest.class);
        converter.setTypeIdMappings(typeMappings);

        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(@Qualifier("jmsConnectionFactory")ConnectionFactory connectionFactory,
                                   MessageConverter jacksonJmsMessageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter);
        template.setExplicitQosEnabled(true);
        template.setDeliveryPersistent(true);
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("jmsConnectionFactory")ConnectionFactory connectionFactory,
                                                                            MessageConverter jacksonJmsMessageConverter,
                                                                            JmsQueueProperties properties) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setConcurrency(properties.getListener().getConcurrency());
        factory.setSessionTransacted(true);
        return factory;
    }
}
