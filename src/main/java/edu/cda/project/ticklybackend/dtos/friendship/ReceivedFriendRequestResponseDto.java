package edu.cda.project.ticklybackend.dtos.friendship;

import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant une demande d'ami reçue.")
public class ReceivedFriendRequestResponseDto {
    @Schema(description = "ID de la demande d'amitié.")
    private Long friendshipId;

    @Schema(description = "Informations sur l'utilisateur qui a envoyé la demande.")
    private UserSummaryDto sender;

    @Schema(description = "Date à laquelle la demande a été envoyée.")
    private Instant requestedAt;
}