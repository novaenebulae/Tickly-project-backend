package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Membership status within a team.
 */
@Schema(description = "Team membership status.")
public enum TeamMemberStatus {
    /**
     * An invitation was sent but not yet accepted by the user.
     */
    @Schema(description = "Pending invitation.")
    PENDING_INVITATION,

    /**
     * The user accepted the invitation and is an active team member.
     */
    @Schema(description = "Active team member.")
    ACTIVE
}