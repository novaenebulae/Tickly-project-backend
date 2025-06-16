package edu.cda.project.ticklybackend.dtos.structure;

import edu.cda.project.ticklybackend.enums.SeatingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour retourner les informations d'un modèle de zone d'audience.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour la réponse d'un modèle de zone d'audience.")
public class AudienceZoneTemplateResponseDto {

    @Schema(description = "ID unique du modèle de zone d'audience.", example = "1")
    private Long id;

    @Schema(description = "Nom du modèle de zone.", example = "Fosse Debout")
    private String name;

    @Schema(description = "Capacité maximale de cette zone modèle.", example = "500")
    private Integer maxCapacity;

    @Schema(description = "Type de placement pour cette zone.", example = "STANDING")
    private SeatingType seatingType;

    @Schema(description = "Statut d'activité de ce modèle de zone.", example = "true")
    private boolean isActive;

    @Schema(description = "ID de l'espace physique (StructureArea) parent.", example = "10")
    private Long areaId;
}