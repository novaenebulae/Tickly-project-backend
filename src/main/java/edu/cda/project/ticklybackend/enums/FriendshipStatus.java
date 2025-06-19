package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Énumération représentant les différents statuts d'une relation d'amitié.
 */
@Schema(description = "Statut d'une relation d'amitié.")
public enum FriendshipStatus {
    /**
     * [cite_start]Une demande d'ami a été envoyée mais n'a pas encore été acceptée ou refusée. [cite: 359]
     */
    @Schema(description = "Demande en attente.")
    PENDING,

    /**
     * [cite_start]La demande d'ami a été acceptée. [cite: 359]
     */
    @Schema(description = "Amis.")
    ACCEPTED,

    /**
     * [cite_start]La demande d'ami a été refusée par le destinataire. [cite: 359]
     */
    @Schema(description = "Demande rejetée.")
    REJECTED,

    /**
     * [cite_start]L'un des utilisateurs a bloqué l'autre, empêchant toute interaction. [cite: 359]
     */
    @Schema(description = "Utilisateur bloqué.")
    BLOCKED,

    /**
     * [cite_start]La demande d'ami a été annulée par l'émetteur avant d'être acceptée ou refusée. [cite: 359]
     */
    @Schema(description = "Demande annulée par l'émetteur.")
    CANCELLED_BY_SENDER
}