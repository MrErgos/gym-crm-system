package io.github.mrergos.gymcrm.config;


import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

import java.time.LocalDate;
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
    public JmsTemplate jmsTemplate(@Qualifier("jmsConnectionFactory") ConnectionFactory connectionFactory,
                                   MessageConverter jacksonJmsMessageConverter,
                                   @Value("${spring.jms.template.delivery-mode:persistent}") String deliveryMode) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter);
        template.setExplicitQosEnabled(true);
        template.setDeliveryPersistent("persistent".equalsIgnoreCase(deliveryMode));
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("jmsConnectionFactory") ConnectionFactory connectionFactory,
                                                                          MessageConverter jacksonJmsMessageConverter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setSessionTransacted(true);
        return factory;
    }
}