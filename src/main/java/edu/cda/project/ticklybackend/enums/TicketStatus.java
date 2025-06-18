package edu.cda.project.ticklybackend.enums;

/**
 * Définit les statuts possibles pour un billet.
 */
public enum TicketStatus {
    /**
     * Le billet est valide et peut être utilisé pour entrer.
     */
    VALID,

    /**
     * Le billet a été scanné et utilisé.
     */
    USED,

    /**
     * Le billet a été annulé (ex: événement annulé, annulation par l'utilisateur).
     */
    CANCELLED,

    /**
     * Le billet concerne un événement passé et est donc expiré.
     */
    EXPIRED
}