package edu.cda.project.ticklybackend.services.files;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
// Si vous n'utilisez pas @Component ici, vous aurez besoin d'une classe @Configuration
// avec @EnableConfigurationProperties(FileStorageProperties.class)
// @Component // Optionnel, si vous préférez cela à @EnableConfigurationProperties dans une classe @Configuration

@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileStorageProperties {

    /**
     * Répertoire racine où les fichiers uploadés seront stockés.
     * Exemple : /var/www/tickly_uploads ou./uploads_dev pour le développement.
     */
    private String uploadDir;

    /**
     * URL de base publique à partir de laquelle les fichiers statiques sont servis (par Nginx).
     * Exemple : http://localhost/static ou https://api.tickly.com/static.
     */
    private String staticBaseUrl;
}