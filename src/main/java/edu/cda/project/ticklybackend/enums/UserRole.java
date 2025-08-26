package edu.cda.project.ticklybackend.enums;

/**
 * Enumeration of user roles within the Tickly application.
 */
public enum UserRole {
    SPECTATOR, // Standard user
    STRUCTURE_ADMINISTRATOR, // Administrator of a structure
    ORGANIZATION_SERVICE, // Team member managing event organization
    RESERVATION_SERVICE, // Team member managing reservations/validation
}