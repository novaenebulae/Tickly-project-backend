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
@Schema(description = "DTO representing a received friend request.")
public class ReceivedFriendRequestResponseDto {
    @Schema(description = "Friend request ID.")
    private Long friendshipId;

    @Schema(description = "Information about the user who sent the request.")
    private UserSummaryDto sender;

    @Schema(description = "Date when the request was sent.")
    private ZonedDateTime requestedAt;
}