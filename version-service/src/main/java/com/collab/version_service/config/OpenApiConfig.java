package com.collab.version_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI versionServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Version Control Service API")
                        .description("RESTful API for maintaining version history, reverting documents, and tracking user contributions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Collaborative Editing System")
                                .email("support@collab.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

