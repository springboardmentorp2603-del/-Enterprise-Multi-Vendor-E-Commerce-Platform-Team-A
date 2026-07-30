package com.shopstack.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shopStackOpenAPI() {

        return new OpenAPI()

                .info(new Info().title("ShopStack API")
                                .description("Enterprise Multi-Vendor E-Commerce Platform REST API")
                                .version("1.0.0")
                                .contact(new Contact()
                                                .name("ShopStack Team")
                                                .email("support@shopstack.com")
                                )
                                .license(new License().name("Apache 2.0"))
                )

                .externalDocs(
                        new ExternalDocumentation()
                                .description("Project Documentation")
                                .url("https://github.com/yourusername/shopstack")
                );
    }

}