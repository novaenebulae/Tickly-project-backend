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
 * DTO for creating and responding with audience zones of an event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAudienceZoneDto {

    @Schema(description = "Audience zone configuration ID (response only).", readOnly = true)
    private Long id;

    @Schema(description = "Zone name for this event.", example = "Gold Pit")
    @NotBlank(message = "Le nom de la zone ne peut pas être vide.")
    private String name;

    @Schema(description = "Maximum capacity of the zone for this event.", example = "500")
    @NotNull(message = "La capacité maximale est requise.")
    @Min(value = 0, message = "La capacité maximale ne peut pas être négative.")
    private Integer allocatedCapacity;

    @Schema(description = "Remaining seats for the zone in this event.", example = "500")
    private Integer remainingCapacity;

    @Schema(description = "Seating type in the zone.", example = "STANDING")
    @NotNull(message = "Le type de placement est requis.")
    private SeatingType seatingType;

    @Schema(description = "Indicates whether the zone is active for ticket sales.", example = "true")
    @NotNull(message = "Le statut actif est requis.")
    private Boolean isActive;

    private Long areaId;

    /**
     * The ID of the template on which this zone is based.
     */
    private Long templateId;
}