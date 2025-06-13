package edu.cda.project.ticklybackend.config;

import edu.cda.project.ticklybackend.services.files.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageConfig {
    // Cette classe permet à Spring de trouver et d'utiliser FileStorageProperties.
    // Elle peut rester vide si son seul rôle est d'activer @ConfigurationProperties.
}