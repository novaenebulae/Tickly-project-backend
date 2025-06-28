package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.enums.SeatingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création et la réponse des zones d'audience d'un événement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAudienceZoneDto {

    @Schema(description = "ID de la configuration de la zone (uniquement en réponse).", readOnly = true)
    private Long id;

    @Schema(description = "Nom de la zone pour cet événement.", example = "Fosse Or")
    @NotBlank(message = "Le nom de la zone ne peut pas être vide.")
    private String name;

    @Schema(description = "Capacité maximale de la zone pour cet événement.", example = "500")
    @NotNull(message = "La capacité maximale est requise.")
    @Min(value = 0, message = "La capacité maximale ne peut pas être négative.")
    private Integer allocatedCapacity;

    @Schema(description = "Type de placement dans la zone.", example = "STANDING")
    @NotNull(message = "Le type de placement est requis.")
    private SeatingType seatingType;

    @Schema(description = "Indique si la zone est active pour la vente de billets.", example = "true")
    @NotNull(message = "Le statut actif est requis.")
    private Boolean isActive;

    private Long areaId;

    /**
     * L'ID du modèle (template) sur lequel cette zone est basée.
     */
    private Long templateId;
}