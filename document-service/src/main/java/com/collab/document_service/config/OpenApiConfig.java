package com.collab.document_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Document Editing Service API")
                        .description("RESTful API for creating, editing, and managing documents with real-time collaboration support")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Collaborative Editing System")
                                .email("support@collab.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

