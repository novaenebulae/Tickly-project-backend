package edu.cda.project.ticklybackend.files;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

// @ConfigurationProperties permet de lier les propriétés préfixées par "file"
// dans application.properties à cette classe.
@Getter
@Setter
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    // Chemin du répertoire où les fichiers uploadés seront stockés.
    // Sera injecté depuis la propriété 'file.upload-dir'.
    private String uploadDir;

    // URL de base pour accéder aux fichiers statiques via le serveur web (Nginx).
    // Sera injecté depuis la propriété 'file.static-base-url'.
    private String staticBaseUrl;
}