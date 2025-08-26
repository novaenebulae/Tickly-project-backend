package edu.cda.project.ticklybackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI/Swagger configuration for the Tickly backend.
 * <p>
 * Declares API metadata, server definitions, and the JWT bearer authentication scheme.
 */
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Tickly Support",
                        email = "contact@tickly.com",
                        url = "https://tickly.com"
                ),
                description = "RESTful API documentation for the Tickly ticketing backend.",
                title = "Tickly API v1.0",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "Local Environment",
                        url = "/"
                ),
                @Server(
                        description = "Production Environment",
                        url = "https://api.tickly.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT bearer token authentication",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
/**
 * Holds OpenAPI annotations; no additional logic is required here.
 */
public class OpenApiConfig {
}