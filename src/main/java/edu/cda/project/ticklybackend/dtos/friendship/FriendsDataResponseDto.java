package edu.cda.project.ticklybackend.dtos.friendship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated friendship data for a user.")
public class FriendsDataResponseDto {

    @Schema(description = "List of accepted friends.")
    private List<FriendResponseDto> friends;

    @Schema(description = "List of received and pending friend requests.")
    private List<ReceivedFriendRequestResponseDto> pendingRequests;

    @Schema(description = "List of sent and pending friend requests.")
    private List<SentFriendRequestResponseDto> sentRequests;
}