package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import edu.cda.project.ticklybackend.services.files.FileStorageProperties;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootStorageLocation; // Répertoire racine des uploads
    private final String staticBaseUrl;     // URL de base pour servir les fichiers

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        LoggingUtils.logMethodEntry(log, "FileStorageServiceImpl", "fileStorageProperties", fileStorageProperties);

        try {
            log.debug("Initialisation du service de stockage de fichiers");

            if (!StringUtils.hasText(fileStorageProperties.getUploadDir())) {
                log.error("La propriété 'file.upload-dir' n'est pas configurée");
                throw new FileStorageException("La propriété 'file.upload-dir' ne peut pas être vide.");
            }
            if (!StringUtils.hasText(fileStorageProperties.getStaticBaseUrl())) {
                log.error("La propriété 'file.static-base-url' n'est pas configurée");
                throw new FileStorageException("La propriété 'file.static-base-url' ne peut pas être vide.");
            }

            this.rootStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            this.staticBaseUrl = fileStorageProperties.getStaticBaseUrl().endsWith("/") ?
                    fileStorageProperties.getStaticBaseUrl() :
                    fileStorageProperties.getStaticBaseUrl() + "/";

            log.debug("Répertoire racine de stockage configuré: {}", this.rootStorageLocation);
            log.debug("URL de base statique configurée: {}", this.staticBaseUrl);

            try {
                Files.createDirectories(this.rootStorageLocation);
                log.info("Service de stockage de fichiers initialisé avec succès. Répertoire: {}", this.rootStorageLocation);
            } catch (Exception ex) {
                LoggingUtils.logException(log, "Impossible de créer le répertoire racine de stockage des fichiers: " + this.rootStorageLocation, ex);
                throw new FileStorageException("Impossible de créer le répertoire racine de stockage des fichiers.", ex);
            }

            LoggingUtils.logMethodExit(log, "FileStorageServiceImpl");
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        LoggingUtils.logMethodEntry(log, "storeFile", "file", file, "subDirectory", subDirectory);

        try {
            log.debug("Début du stockage d'un fichier dans le sous-répertoire: {}", subDirectory);

            if (file.isEmpty()) {
                log.warn("Tentative de stockage d'un fichier vide");
                throw new FileStorageException("Impossible de stocker un fichier vide.");
            }

            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            log.debug("Nom de fichier original: {}", originalFilename);

            String fileExtension = "";
            try {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                log.debug("Extension du fichier: {}", fileExtension);
            } catch (Exception e) {
                // Pas d'extension ou nom de fichier invalide
                log.warn("Nom de fichier invalide ou sans extension: {}", originalFilename);
                throw new FileStorageException("Nom de fichier invalide ou sans extension : " + originalFilename);
            }

            // Générer un nom de fichier unique pour éviter les collisions et les problèmes de sécurité
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            log.debug("Nom de fichier unique généré: {}", uniqueFilename);

            try {
                Path targetDirectory = this.rootStorageLocation.resolve(StringUtils.hasText(subDirectory) ? subDirectory : "").normalize();
                log.debug("Répertoire cible: {}", targetDirectory);

                // Vérifier que le sous-répertoire est bien dans le répertoire racine
                if (!targetDirectory.startsWith(this.rootStorageLocation)) {
                    log.error("Tentative de stockage en dehors du répertoire racine: {}", targetDirectory);
                    throw new FileStorageException("Impossible de stocker le fichier en dehors du répertoire racine configuré.");
                }

                Files.createDirectories(targetDirectory); // Crée le sous-répertoire s'il n'existe pas
                log.debug("Sous-répertoire créé ou vérifié: {}", targetDirectory);

                Path targetLocation = targetDirectory.resolve(uniqueFilename);
                log.debug("Emplacement cible du fichier: {}", targetLocation);

                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Fichier stocké avec succès: {} (taille: {} octets)", uniqueFilename, file.getSize());
                }

                LoggingUtils.logMethodExit(log, "storeFile", uniqueFilename);
                // Retourner le nom de fichier unique (sans le sous-répertoire, car getFileUrl le gère)
                return uniqueFilename;
            } catch (IOException ex) {
                LoggingUtils.logException(log, "Erreur lors du stockage du fichier " + originalFilename, ex);
                throw new FileStorageException("Impossible de stocker le fichier " + originalFilename + ". Veuillez réessayer!", ex);
            }
        } finally {
            LoggingUtils.clearContext();
        }
    }

    private Path buildPath(String filename, String subDirectory) {
        LoggingUtils.logMethodEntry(log, "buildPath", "filename", filename, "subDirectory", subDirectory);

        Path subDirPath = StringUtils.hasText(subDirectory) ? Paths.get(subDirectory) : Paths.get("");
        Path result = this.rootStorageLocation.resolve(subDirPath).resolve(filename).normalize();
        log.trace("Chemin construit pour le fichier {} dans le sous-répertoire {}: {}", filename, subDirectory, result);

        LoggingUtils.logMethodExit(log, "buildPath", result);
        return result;
    }

    @Override
    public Path loadFile(String filename, String subDirectory) {
        LoggingUtils.logMethodEntry(log, "loadFile", "filename", filename, "subDirectory", subDirectory);

        try {
            log.debug("Chargement du chemin pour le fichier {} dans le sous-répertoire {}", filename, subDirectory);
            Path path = buildPath(filename, subDirectory);
            log.debug("Chemin résolu: {}", path);

            LoggingUtils.logMethodExit(log, "loadFile", path);
            return path;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public Resource loadFileAsResource(String filename, String subDirectory) {
        LoggingUtils.logMethodEntry(log, "loadFileAsResource", "filename", filename, "subDirectory", subDirectory);

        try {
            log.debug("Chargement du fichier {} depuis le sous-répertoire {}", filename, subDirectory);
            try {
                Path filePath = loadFile(filename, subDirectory);
                log.debug("Chemin du fichier à charger: {}", filePath);

                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() || resource.isReadable()) {
                    log.debug("Fichier trouvé et lisible: {}", filename);
                    LoggingUtils.logMethodExit(log, "loadFileAsResource", resource);
                    return resource;
                } else {
                    log.warn("Fichier non trouvé ou non lisible: {}", filePath);
                    throw new FileStorageException("Impossible de lire le fichier : " + filename);
                }
            } catch (MalformedURLException ex) {
                LoggingUtils.logException(log, "URL malformée lors du chargement du fichier " + filename, ex);
                throw new FileStorageException("Impossible de lire le fichier : " + filename, ex);
            }
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void deleteFile(String filename, String subDirectory) throws FileStorageException {
        LoggingUtils.logMethodEntry(log, "deleteFile", "filename", filename, "subDirectory", subDirectory);

        try {
            if (filename == null || filename.isEmpty()) {
                log.debug("Tentative de suppression d'un fichier avec un nom vide ou null");
                return; // Ne rien faire si le nom de fichier est vide ou null
            }

            log.debug("Suppression du fichier {} dans le sous-répertoire {}", filename, subDirectory);
            try {
                Path filePath = loadFile(filename, subDirectory);
                log.debug("Chemin du fichier à supprimer: {}", filePath);

                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    log.info("Fichier supprimé avec succès: {}", filePath);
                } else {
                    log.warn("Fichier non trouvé lors de la tentative de suppression: {}", filePath);
                }

                LoggingUtils.logMethodExit(log, "deleteFile");
            } catch (IOException ex) {
                LoggingUtils.logException(log, "Erreur lors de la suppression du fichier " + filename, ex);
                throw new FileStorageException("Impossible de supprimer le fichier " + filename + ". Veuillez réessayer!", ex);
            }
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public String getFileUrl(String filename, String subDirectory) {
        LoggingUtils.logMethodEntry(log, "getFileUrl", "filename", filename, "subDirectory", subDirectory);

        try {
            log.debug("Génération de l'URL pour le fichier {} dans le sous-répertoire {}", filename, subDirectory);

            if (!StringUtils.hasText(filename)) {
                log.debug("Nom de fichier vide ou null, retour null");
                LoggingUtils.logMethodExit(log, "getFileUrl", null);
                return null; // Ou une URL par défaut / placeholder
            }

            String cleanSubDirectory = (StringUtils.hasText(subDirectory)) ?
                    (subDirectory.endsWith("/") ? subDirectory : subDirectory + "/") :
                    "";
            String cleanFilename = filename.startsWith("/") ? filename.substring(1) : filename;

            String url = this.staticBaseUrl + cleanSubDirectory + cleanFilename;
            log.debug("URL générée: {}", url);

            LoggingUtils.logMethodExit(log, "getFileUrl", url);
            return url;
        } finally {
            LoggingUtils.clearContext();
        }
    }
}
