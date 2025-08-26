package edu.cda.project.ticklybackend.dtos.team;

import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Team member representation.")
public class TeamMemberDto {
    @Schema(description = "Membership record ID.")
    private Long id;

    @Schema(description = "User ID (if the account is linked).")
    private Long userId;

    @Schema(description = "User first name.")
    private String firstName;

    @Schema(description = "User last name.")
    private String lastName;

    @Schema(description = "Member email.")
    private String email;

    @Schema(description = "User avatar URL.")
    private String avatarUrl;

    @Schema(description = "Member role in the team.")
    private UserRole role;

    @Schema(description = "Membership status (e.g., PENDING_INVITATION, ACTIVE).")
    private TeamMemberStatus status;

    @Schema(description = "Date when the member joined the team.")
    private ZonedDateTime joinedAt;
}