package io.github.mrergos.workinghours.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI workingHoursOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Working Hours Microservice API")
                        .version("v1")
                        .description("Accepts trainer workload events and returns per-trainer monthly training-hours summaries. " +
                                "Intended for service-to-service use by the Main Gym CRM Microservice."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(BEARER_AUTH_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
