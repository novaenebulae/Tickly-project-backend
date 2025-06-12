package edu.cda.project.ticklybackend.enums;

/**
 * Énumération des rôles utilisateurs au sein de l'application Tickly.
 */
public enum UserRole {
    SPECTATOR, // Utilisateur standard
    STRUCTURE_ADMINISTRATOR, // Administrateur d'une structure
    ORGANIZATION_SERVICE, // Membre d'équipe gérant l'organisation des événements
    RESERVATION_SERVICE, // Membre d'équipe gérant les réservations/validation
}