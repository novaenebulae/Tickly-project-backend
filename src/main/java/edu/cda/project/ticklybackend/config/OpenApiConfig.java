package edu.cda.project.ticklybackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Support Tickly",
                        email = "contact@tickly.com",
                        url = "https://tickly.com"
                ),
                description = "Documentation de l'API RESTful pour le backend du système de billetterie Tickly.",
                title = "API Tickly v1.0",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "Environnement Local",
                        url = "/"
                ),
                @Server(
                        description = "Environnement de Production",
                        url = "https://api.tickly.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Authentification par token JWT",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}