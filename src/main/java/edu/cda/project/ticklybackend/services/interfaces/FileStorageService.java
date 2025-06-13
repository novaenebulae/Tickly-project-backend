package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Stocke un fichier dans un sous-répertoire spécifié.
     *
     * @param file         Le fichier à stocker.
     * @param subDirectory Le sous-répertoire (ex: "avatars", "structures/logos").
     * @return Le nom de fichier unique (ou chemin relatif à partir de uploadDir/subDirectory) sous lequel le fichier a été stocké.
     */
    String storeFile(MultipartFile file, String subDirectory);

    /**
     * Charge un fichier en tant que chemin.
     *
     * @param filename     Le nom du fichier à charger (tel que retourné par storeFile).
     * @param subDirectory Le sous-répertoire où le fichier est stocké.
     * @return Le chemin vers le fichier.
     */
    Path loadFile(String filename, String subDirectory);

    /**
     * Charge un fichier en tant que ressource Spring.
     *
     * @param filename     Le nom du fichier à charger.
     * @param subDirectory Le sous-répertoire.
     * @return La ressource.
     */
    Resource loadFileAsResource(String filename, String subDirectory);

    /**
     * Supprime un fichier.
     *
     * @param filename     Le nom du fichier à supprimer.
     * @param subDirectory Le sous-répertoire.
     */
    void deleteFile(String filename, String subDirectory) throws FileStorageException;

    /**
     * Construit l'URL publique complète pour accéder à un fichier.
     *
     * @param filename     Le nom du fichier (tel que stocké, ex: "uuid.jpg").
     * @param subDirectory Le sous-répertoire (ex: "avatars").
     * @return L'URL complète.
     */
    String getFileUrl(String filename, String subDirectory);
}