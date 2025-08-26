package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of roles a user can have within a structure's team.
 * These roles are a subset of the global UserRole values.
 */
@Schema(description = "Member role within a structure's team.")
public enum TeamRole {
    /**
     * Structure administrator with the highest permissions on the structure and its team.
     * Corresponds to UserRole.STRUCTURE_ADMINISTRATOR.
     */
    @Schema(description = "Structure administrator.")
    ADMIN,

    /**
     * Team member responsible for managing events.
     * Corresponds to UserRole.ORGANIZATION_SERVICE.
     */
    @Schema(description = "Team member managing event organization.")
    ORGANIZER,

    /**
     * Team member responsible for managing reservations and ticket validation.
     * Corresponds to UserRole.RESERVATION_SERVICE.
     */
    @Schema(description = "Team member managing reservations and validation.")
    RESERVATION
}