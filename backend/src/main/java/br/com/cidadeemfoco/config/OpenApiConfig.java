package br.com.cidadeemfoco.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cidade em Foco API",
                version = "1.0.0",
                description = "API REST do Cidade em Foco para Praia Grande-SP"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Informe o token JWT obtido em /api/auth/login"
)
public class OpenApiConfig {

    @Bean
    GroupedOpenApi publicApiV1() {
        return GroupedOpenApi.builder()
                .group("public-v1")
                .displayName("API pública v1")
                .pathsToMatch("/api/v1/public/**", "/actuator/health")
                .build();
    }
}
