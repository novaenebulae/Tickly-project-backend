package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour retourner les informations d'un espace physique (StructureArea).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour la réponse d'un espace physique (StructureArea).")
public class AreaResponseDto {

    @Schema(description = "ID unique de l'espace physique.", example = "10")
    private Long id;

    @Schema(description = "Nom de l'espace physique.", example = "Salle A")
    private String name;

    @Schema(description = "Description de cet espace (optionnel).", example = "Salle principale avec scène.")
    private String description;

    @Schema(description = "Capacité maximale de l'espace.", example = "1200")
    private Integer maxCapacity;

    @Schema(description = "Statut d'activité de l'espace.", example = "true")
    private boolean isActive;

    @Schema(description = "ID de la structure parente.", example = "1")
    private Long structureId;

    @Schema(description = "Liste des modèles de zones d'audience associés à cet espace.")
    private List<AudienceZoneTemplateResponseDto> audienceZoneTemplates;
}