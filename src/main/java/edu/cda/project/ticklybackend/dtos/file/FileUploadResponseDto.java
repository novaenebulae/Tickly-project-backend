package edu.cda.project.ticklybackend.dtos.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse suite à un upload de fichier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour la réponse suite à un upload de fichier.")
public class FileUploadResponseDto {

    @Schema(description = "Nom original du fichier uploadé.", example = "mon_logo.png")
    private String fileName;

    @Schema(description = "URL publique complète pour accéder au fichier uploadé.", example = "http://localhost/static/structures/logos/uuid-logo.png")
    private String fileUrl;

    @Schema(description = "Message confirmant le succès de l'upload.", example = "Fichier uploadé avec succès.")
    private String message;
}