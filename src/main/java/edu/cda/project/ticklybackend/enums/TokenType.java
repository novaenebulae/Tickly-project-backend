package edu.cda.project.ticklybackend.enums;

/**
 * Enumeration of verification token types used in the application.
 * Each type corresponds to a specific user action.
 */
public enum TokenType {
    /**
     * Token sent to validate a new user's email address.
     */
    EMAIL_VALIDATION,

    /**
     * Token sent to allow resetting a forgotten password.
     */
    PASSWORD_RESET,

    /**
     * Token sent to invite a user to join a team.
     */
    TEAM_INVITATION,

    /**
     * Token sent to confirm account deletion.
     */
    ACCOUNT_DELETION_CONFIRMATION
}