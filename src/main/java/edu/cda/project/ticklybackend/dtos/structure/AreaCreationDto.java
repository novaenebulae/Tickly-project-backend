package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un espace physique (StructureArea).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour la création d'un espace physique (StructureArea).")
public class AreaCreationDto {

    @NotBlank(message = "Le nom de l'espace ne peut pas être vide.")
    @Size(max = 255, message = "Le nom de l'espace ne doit pas dépasser 255 caractères.")
    @Schema(description = "Nom de l'espace physique.", example = "Salle A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Description de cet espace (optionnel).", example = "Salle principale avec scène.")
    private String description;

    @NotNull(message = "La capacité maximale ne peut pas être nulle.")
    @Min(value = 0, message = "La capacité maximale doit être un nombre positif ou zéro.")
    @Schema(description = "Capacité maximale de l'espace.", example = "1200", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCapacity;

    @Schema(description = "Statut d'activité de l'espace. Par défaut à true si non fourni.", example = "true")
    private Boolean isActive = true;
}