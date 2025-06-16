package edu.cda.project.ticklybackend.dtos.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour afficher une liste résumée de structures.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO pour un résumé de structure, utilisé dans les listes.")
public class StructureSummaryDto {

    @Schema(description = "ID unique de la structure.", example = "1")
    private Long id;

    @Schema(description = "Nom de la structure.", example = "Le Grand Théâtre de la Ville")
    private String name;

    @Schema(description = "Liste des types auxquels la structure appartient.")
    private List<StructureTypeDto> types;

    @Schema(description = "Ville où se situe la structure.", example = "Paris")
    private String city; // Extrait de l'adresse

    @Schema(description = "URL complète du logo de la structure (optionnel).", example = "http://localhost/static/structures/logos/uuid-logo.jpg")
    private String logoUrl;

    @Schema(description = "URL complète de l'image de couverture de la structure (optionnel).", example = "http://localhost/static/structures/covers/uuid-cover.jpg")
    private String coverUrl;

    @Schema(description = "Indicateur du statut d'activité de la structure.", example = "true")
    private boolean isActive;

    @Schema(description = "Nombre d'événements actifs ou à venir associés à cette structure (optionnel).", example = "15")
    private Integer eventCount; // Optionnel, pourrait être calculé ou stocké
}