package edu.cda.project.ticklybackend.dtos.friendship;

import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing an accepted friend.")
public class FriendResponseDto {

    @Schema(description = "Friendship relation ID.", example = "42")
    private Long friendshipId;

    @Schema(description = "Information about the user who is the friend.")
    private UserSummaryDto friend;

    @Schema(description = "Date when the friendship became effective.", example = "2025-06-25T10:30:00Z")
    private ZonedDateTime since;
}