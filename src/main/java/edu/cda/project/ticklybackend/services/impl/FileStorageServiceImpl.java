package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import edu.cda.project.ticklybackend.services.files.FileStorageProperties;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
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
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootStorageLocation; // Répertoire racine des uploads
    private final String staticBaseUrl;     // URL de base pour servir les fichiers

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        if (!StringUtils.hasText(fileStorageProperties.getUploadDir())) {
            throw new FileStorageException("La propriété 'file.upload-dir' ne peut pas être vide.");
        }
        if (!StringUtils.hasText(fileStorageProperties.getStaticBaseUrl())) {
            throw new FileStorageException("La propriété 'file.static-base-url' ne peut pas être vide.");
        }

        this.rootStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.staticBaseUrl = fileStorageProperties.getStaticBaseUrl().endsWith("/") ?
                fileStorageProperties.getStaticBaseUrl() :
                fileStorageProperties.getStaticBaseUrl() + "/";

        try {
            Files.createDirectories(this.rootStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Impossible de créer le répertoire racine de stockage des fichiers.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new FileStorageException("Impossible de stocker un fichier vide.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } catch (Exception e) {
            // Pas d'extension ou nom de fichier invalide
            throw new FileStorageException("Nom de fichier invalide ou sans extension : " + originalFilename);
        }

        // Générer un nom de fichier unique pour éviter les collisions et les problèmes de sécurité
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetDirectory = this.rootStorageLocation.resolve(StringUtils.hasText(subDirectory) ? subDirectory : "").normalize();
            // Vérifier que le sous-répertoire est bien dans le répertoire racine
            if (!targetDirectory.startsWith(this.rootStorageLocation)) {
                throw new FileStorageException("Impossible de stocker le fichier en dehors du répertoire racine configuré.");
            }
            Files.createDirectories(targetDirectory); // Crée le sous-répertoire s'il n'existe pas

            Path targetLocation = targetDirectory.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            // Retourner le nom de fichier unique (sans le sous-répertoire, car getFileUrl le gère)
            return uniqueFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de stocker le fichier " + originalFilename + ". Veuillez réessayer!", ex);
        }
    }

    private Path buildPath(String filename, String subDirectory) {
        Path subDirPath = StringUtils.hasText(subDirectory) ? Paths.get(subDirectory) : Paths.get("");
        return this.rootStorageLocation.resolve(subDirPath).resolve(filename).normalize();
    }

    @Override
    public Path loadFile(String filename, String subDirectory) {
        return buildPath(filename, subDirectory);
    }

    @Override
    public Resource loadFileAsResource(String filename, String subDirectory) {
        try {
            Path filePath = loadFile(filename, subDirectory);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Impossible de lire le fichier : " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Impossible de lire le fichier : " + filename, ex);
        }
    }

    @Override
    public void deleteFile(String filename, String subDirectory) throws FileStorageException {
        try {
            Path filePath = loadFile(filename, subDirectory);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de supprimer le fichier " + filename + ". Veuillez réessayer!", ex);
        }
    }

    @Override
    public String getFileUrl(String filename, String subDirectory) {
        if (!StringUtils.hasText(filename)) {
            return null; // Ou une URL par défaut / placeholder
        }
        String cleanSubDirectory = (StringUtils.hasText(subDirectory)) ?
                (subDirectory.endsWith("/") ? subDirectory : subDirectory + "/") :
                "";
        String cleanFilename = filename.startsWith("/") ? filename.substring(1) : filename;
        return this.staticBaseUrl + cleanSubDirectory + cleanFilename;
    }
}