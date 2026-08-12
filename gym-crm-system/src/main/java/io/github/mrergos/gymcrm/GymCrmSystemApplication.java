package io.github.mrergos.gymcrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "io.github.mrergos.gymcrm.integration")
public class GymCrmSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(GymCrmSystemApplication.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM System application...");
        SpringApplication.run(GymCrmSystemApplication.class, args);
        log.info("Gym CRM application finished successfully.");
    }

}
