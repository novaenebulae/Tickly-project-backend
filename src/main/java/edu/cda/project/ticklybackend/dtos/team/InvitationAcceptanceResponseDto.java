package edu.cda.project.ticklybackend.dtos.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Réponse retournée après l'acceptation d'une invitation à une équipe.")
public class InvitationAcceptanceResponseDto {

    @Schema(description = "Nouveau token JWT avec les permissions mises à jour.")
    private String accessToken;

    @Schema(description = "Durée de validité du token en millisecondes.")
    private Long expiresIn;

    @Schema(description = "ID de la structure à laquelle l'utilisateur a été associé.")
    private Long structureId;

    @Schema(description = "Nom de la structure.")
    private String structureName;

    @Schema(description = "Message de confirmation.")
    private String message;
}