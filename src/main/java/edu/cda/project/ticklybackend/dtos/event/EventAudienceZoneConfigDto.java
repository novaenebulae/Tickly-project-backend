package edu.cda.project.ticklybackend.dtos.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO used for creating or updating the configuration of an audience zone for a specific event.
 * It links a reusable AudienceZoneTemplate to an event with a specific capacity.
 */
@Data
public class EventAudienceZoneConfigDto {

    /**
     * The ID of the existing EventAudienceZone to update.
     * This field should be null when creating a new zone configuration for an event.
     */
    private Long id;

    /**
     * The ID of the AudienceZoneTemplate to use as a base. This is mandatory.
     */
    @NotNull(message = "Template ID cannot be null.")
    private Long templateId;

    /**
     * The specific capacity allocated for this zone for this event.
     * Must be a positive number.
     */
    @NotNull(message = "Allocated capacity cannot be null.")
    @Min(value = 1, message = "Allocated capacity must be at least 1.")
    private Integer allocatedCapacity;

}
