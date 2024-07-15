package com.valr.assignment.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Order API")
                    .version("1.0")
                    .description("API for managing orders")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )
    }
}
