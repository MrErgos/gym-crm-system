package io.github.mrergos.gymcrm.component.config;

import io.cucumber.spring.CucumberContextConfiguration;
import io.github.mrergos.gymcrm.GymCrmSystemApplication;
import io.github.mrergos.gymcrm.integration.ReplyQueueListenerRegistrar;
import io.github.mrergos.gymcrm.integration.WorkingHoursGateway;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmSystemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("component-test")
public class CucumberSpringConfiguration {
    @MockitoBean
    private WorkingHoursGateway workingHoursGateway;

    @MockitoBean
    private ReplyQueueListenerRegistrar replyQueueListenerRegistrar;
}