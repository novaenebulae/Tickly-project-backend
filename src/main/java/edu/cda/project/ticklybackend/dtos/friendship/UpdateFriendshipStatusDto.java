package edu.cda.project.ticklybackend.dtos.friendship;

import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Payload to update the status of a friend request.")
public class UpdateFriendshipStatusDto {
    @NotNull
    @Schema(description = "New friendship status (e.g., ACCEPTED, REJECTED, CANCELLED_BY_SENDER).", requiredMode = Schema.RequiredMode.REQUIRED)
    private FriendshipStatus status;
}