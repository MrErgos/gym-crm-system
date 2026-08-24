package io.github.mrergos.workinghours;

import io.github.mrergos.workinghours.config.JmsQueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(JmsQueueProperties.class)
public class WorkingHoursServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(WorkingHoursServiceApplication.class);

    public static void main(String[] args) {
        log.info("Starting Working Hours microservice...");
        SpringApplication.run(WorkingHoursServiceApplication.class, args);
        log.info("Working Hours microservice started successfully.");
    }
}

