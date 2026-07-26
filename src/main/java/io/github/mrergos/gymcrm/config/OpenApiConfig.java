package io.github.mrergos.gymcrm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH_SCHEME_NAME = "basicAuth";

    @Bean
    public OpenAPI gymCrmOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym CRM System API")
                        .version("v1")
                        .description("REST API for managing Trainees, Trainers and Trainings"))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(BASIC_AUTH_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
    }
}