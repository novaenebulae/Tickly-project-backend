package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Payload to update an event status.
 */
@Data
public class EventStatusUpdateDto {
    @Schema(description = "New event status.", example = "PUBLISHED")
    @NotNull(message = "Le statut ne peut pas être nul.")
    private EventStatus status;
}