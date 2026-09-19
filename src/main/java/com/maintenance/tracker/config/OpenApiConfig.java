package com.maintenance.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maintenance Work-Order Tracker API")
                        .version("v1.0.0")
                        .description("RESTful API for tracking industrial equipment maintenance work orders, "
                                + "status progression, technician assignments, and dynamic filtering."));
    }
}