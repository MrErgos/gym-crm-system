package io.github.mrergos.gymcrm.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "io.github.mrergos.gymcrm")
@PropertySource("classpath:application.properties")
@Import({HibernateConfig.class, WebConfig.class, OpenApiConfig.class})
@EnableTransactionManagement
public class Config {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
