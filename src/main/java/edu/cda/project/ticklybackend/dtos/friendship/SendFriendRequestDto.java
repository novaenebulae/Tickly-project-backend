package edu.cda.project.ticklybackend.dtos.friendship;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO pour envoyer une demande d'ami.")
public class SendFriendRequestDto {
    @NotNull(message = "L'ID du destinataire ne peut pas être nul.")
    @Schema(description = "ID de l'utilisateur à qui envoyer la demande.", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long receiverId;
}