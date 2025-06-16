package edu.cda.project.ticklybackend.dtos.structure;

import edu.cda.project.ticklybackend.enums.SeatingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un modèle de zone d'audience.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour la création d'un modèle de zone d'audience.")
public class AudienceZoneTemplateUpdateDto {

    @Size(max = 255, message = "Le nom du modèle de zone ne doit pas dépasser 255 caractères.")
    @Schema(description = "Nom du modèle de zone.", example = "Fosse Debout", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Min(value = 0, message = "La capacité maximale doit être un nombre positif ou zéro.")
    @Schema(description = "Capacité maximale de cette zone modèle.", example = "500", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCapacity;

    @Schema(description = "Type de placement pour cette zone. MIXED par défaut si non fourni", example = "STANDING")
    private SeatingType seatingType;

    @Schema(description = "Statut d'activité de ce modèle de zone. Par défaut à true si non fourni.", example = "true")
    private Boolean isActive;
}