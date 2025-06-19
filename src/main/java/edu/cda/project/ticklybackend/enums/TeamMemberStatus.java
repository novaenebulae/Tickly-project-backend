package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Statut d'un membre au sein d'une équipe.
 */
@Schema(description = "Statut d'un membre au sein d'une équipe.")
public enum TeamMemberStatus {
    /**
     * Une invitation a été envoyée, mais pas encore acceptée par l'utilisateur.
     */
    @Schema(description = "Invitation en attente d'acceptation.")
    PENDING_INVITATION,

    /**
     * L'utilisateur a accepté l'invitation et fait partie activement de l'équipe.
     */
    @Schema(description = "Membre actif de l'équipe.")
    ACTIVE
}