package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour la mise à jour d'un espace physique (StructureArea).
 * Tous les champs sont optionnels pour permettre des mises à jour partielles.
 */
@Data
@Schema(description = "DTO pour la mise à jour d'un espace physique (StructureArea).")
public class AreaUpdateDto {

    @Size(max = 255, message = "Le nom de l'espace ne doit pas dépasser 255 caractères.")
    @Schema(description = "Nouveau nom de l'espace physique.", example = "Salle B")
    private String name;

    @Schema(description = "Nouvelle description de cet espace.", example = "Salle secondaire, plus intime.")
    private String description;

    @Min(value = 0, message = "La capacité maximale doit être un nombre positif ou zéro.")
    @Schema(description = "Nouvelle capacité maximale de l'espace.", example = "300")
    private Integer maxCapacity;

    @Schema(description = "Nouveau statut d'activité de l'espace.", example = "false")
    private Boolean isActive;
}
