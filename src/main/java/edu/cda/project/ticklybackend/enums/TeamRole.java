package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Énumération des rôles qu'un utilisateur peut avoir au sein d'une équipe de structure.
 * Ces rôles sont un sous-ensemble des UserRole globaux.
 */
@Schema(description = "Rôle d'un membre au sein de l'équipe d'une structure.")
public enum TeamRole {
    /**
     * Administrateur de la structure, avec les droits les plus élevés sur la structure et son équipe.
     * Corresponds à UserRole.STRUCTURE_ADMINISTRATOR.
     */
    @Schema(description = "Administrateur de la structure.")
    ADMIN,

    /**
     * Membre d'équipe responsable de la gestion des événements.
     * Corresponds à UserRole.ORGANIZATION_SERVICE.
     */
    @Schema(description = "Membre de l'équipe gérant l'organisation des événements.")
    ORGANIZER,

    /**
     * Membre d'équipe responsable de la gestion des réservations et de la validation des billets.
     * Corresponds à UserRole.RESERVATION_SERVICE.
     */
    @Schema(description = "Membre de l'équipe gérant les réservations et la validation.")
    RESERVATION
}