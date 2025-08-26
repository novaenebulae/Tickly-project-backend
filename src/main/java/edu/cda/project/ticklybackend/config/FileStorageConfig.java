package edu.cda.project.ticklybackend.config;

import edu.cda.project.ticklybackend.services.files.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that enables binding of file storage properties from application configuration
 * to the FileStorageProperties class.
 */
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageConfig {
}