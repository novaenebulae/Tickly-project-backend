package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la mise à jour du statut d'un événement.
 */
@Data
public class EventStatusUpdateDto {
    @Schema(description = "Le nouveau statut de l'événement.", example = "PUBLISHED")
    @NotNull(message = "Le statut ne peut pas être nul.")
    private EventStatus status;
}