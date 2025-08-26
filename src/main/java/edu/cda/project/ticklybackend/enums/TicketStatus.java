package edu.cda.project.ticklybackend.enums;

/**
 * Defines the possible statuses for a ticket.
 */
public enum TicketStatus {
    /**
     * The ticket is valid and can be used for entry.
     */
    VALID,

    /**
     * The ticket has been scanned and used.
     */
    USED,

    /**
     * The ticket was cancelled (e.g., event cancelled, user cancellation).
     */
    CANCELLED,

    /**
     * The ticket concerns a past event and is therefore expired.
     */
    EXPIRED
}