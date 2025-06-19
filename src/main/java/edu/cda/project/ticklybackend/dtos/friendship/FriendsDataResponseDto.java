package edu.cda.project.ticklybackend.dtos.friendship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO complet contenant toutes les informations d'amitié d'un utilisateur.")
public class FriendsDataResponseDto {

    @Schema(description = "Liste des amis acceptés.")
    private List<FriendResponseDto> friends;

    @Schema(description = "Liste des demandes d'ami reçues et en attente.")
    private List<ReceivedFriendRequestResponseDto> pendingRequests;

    @Schema(description = "Liste des demandes d'ami envoyées et en attente.")
    private List<SentFriendRequestResponseDto> sentRequests;
}