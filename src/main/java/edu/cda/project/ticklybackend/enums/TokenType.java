package edu.cda.project.ticklybackend.enums;

/**
 * Énumération des différents types de tokens de vérification utilisés dans l'application.
 * Chaque type correspond à une action utilisateur spécifique.
 */
public enum TokenType {
    /**
     * Token envoyé pour valider l'adresse e-mail d'un nouvel utilisateur.
     */
    EMAIL_VALIDATION,

    /**
     * Token envoyé pour permettre la réinitialisation d'un mot de passe oublié.
     */
    PASSWORD_RESET,

    /**
     * Token envoyé pour inviter un utilisateur à rejoindre une équipe.
     */
    TEAM_INVITATION,

    /**
     * Token envoyé pour confirmer la suppression d'un compte.
     */
    ACCOUNT_DELETION_CONFIRMATION
}