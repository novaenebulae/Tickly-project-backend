package edu.cda.project.ticklybackend.dtos.friendship;

import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO pour mettre à jour le statut d'une demande d'ami.")
public class UpdateFriendshipStatusDto {
    @NotNull
    @Schema(description = "Nouveau statut de la relation (ex: ACCEPTED, REJECTED, CANCELLED_BY_SENDER).", requiredMode = Schema.RequiredMode.REQUIRED)
    private FriendshipStatus status;
}