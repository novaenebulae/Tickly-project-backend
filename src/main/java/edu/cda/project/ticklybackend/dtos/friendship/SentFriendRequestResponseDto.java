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
@Schema(description = "DTO représentant une demande d'ami envoyée.")
public class SentFriendRequestResponseDto {
    @Schema(description = "ID de la demande d'amitié.")
    private Long friendshipId;

    @Schema(description = "Informations sur l'utilisateur qui a reçu la demande.")
    private UserSummaryDto receiver;

    @Schema(description = "Date à laquelle la demande a été envoyée.")
    private Instant sentAt;
}