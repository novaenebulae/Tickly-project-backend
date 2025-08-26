package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of possible friendship relationship statuses.
 */
@Schema(description = "Friendship relationship status.")
public enum FriendshipStatus {
    /**
     * A friend request was sent and is awaiting a decision.
     */
    @Schema(description = "Pending request.")
    PENDING,

    /**
     * The friend request was accepted.
     */
    @Schema(description = "Accepted.")
    ACCEPTED,

    /**
     * The friend request was rejected by the receiver.
     */
    @Schema(description = "Rejected.")
    REJECTED,

    /**
     * One user has blocked the other, preventing interactions.
     */
    @Schema(description = "Blocked.")
    BLOCKED,

    /**
     * The request was cancelled by the sender before a decision was made.
     */
    @Schema(description = "Cancelled by sender.")
    CANCELLED_BY_SENDER
}