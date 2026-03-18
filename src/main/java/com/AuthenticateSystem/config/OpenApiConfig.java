package com.AuthenticateSystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    // ── API metadata ──────────────────────────────────────────────────────
    private Info apiInfo() {
        return new Info()
                .title("AuthenticateSystem API")
                .description("""
                        Authentication and user management service.
                        
                        **Authentication:** All protected endpoints require a Bearer token
                        in the `Authorization` header. Obtain a token via `POST /api/v1/auth/login`
                        or via OAuth2 (`GET /oauth2/authorize/{provider}`).
                        
                        **Error format:**
                        ```json
                        {
                          "success": false,
                          "code":    "ERROR_CODE",
                          "message": "Message"
                        }
                        ```
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("AuthenticateSystem Team")
                        .email("faken.dev@gmail.com")
                        .url("https://github.com/faken-dev/AuthenticateSystem"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    // ── Servers ───────────────────────────────────────────────────────────
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local development"),
                new Server()
                        .url("https://api.authenticatesystem.com")
                        .description("Production")
        );
    }

    // ── Global security requirement — Bearer token ─────────────────────
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(BEARER_SCHEME);
    }

    // ── Security scheme definition ────────────────────────────────────────
    private Components components() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your access token here. Obtain one from POST /api/v1/auth/login");

        return new Components()
                .addSecuritySchemes(BEARER_SCHEME, bearerScheme);
    }
}