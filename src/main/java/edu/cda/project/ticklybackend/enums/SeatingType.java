package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of seating types for audience zones.
 */
@Schema(description = "Seating type for an audience zone.")
public enum SeatingType {
    @Schema(description = "Seated places, numbered or not.")
    SEATED,
    @Schema(description = "Standing, free placement.")
    STANDING,
    @Schema(description = "Combination of seated and standing, or unspecified.")
    MIXED
}