package edu.cda.project.ticklybackend.config;

import edu.cda.project.ticklybackend.files.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// @Configuration indique que cette classe contient des configurations Spring.
@Configuration
// @EnableConfigurationProperties active le support pour les classes @ConfigurationProperties,
// ici spécifiquement pour FileStorageProperties.
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageConfig {
    // Cette classe peut rester vide si son seul but est d'activer @ConfigurationProperties.
    // Elle peut aussi être utilisée pour définir d'autres beans liés au stockage de fichiers si nécessaire.
}