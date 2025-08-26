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
@Schema(description = "DTO representing a sent friend request.")
public class SentFriendRequestResponseDto {
    @Schema(description = "Friend request ID.")
    private Long friendshipId;

    @Schema(description = "Information about the user who received the request.")
    private UserSummaryDto receiver;

    @Schema(description = "Date when the request was sent.")
    private ZonedDateTime sentAt;
}