package edu.cda.project.ticklybackend.dtos.ticket;

import edu.cda.project.ticklybackend.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse après une tentative de validation de billet.")
public class TicketValidationResponseDto {

    @Schema(description = "ID du billet qui a été validé.")
    private UUID ticketId;

    @Schema(description = "Le statut du billet après la tentative de validation.")
    private TicketStatus status;

    @Schema(description = "Un message lisible par l'homme confirmant le résultat.", example = "Billet validé avec succès.")
    private String message;

    @Schema(description = "Informations sur le participant pour une vérification rapide.")
    private ParticipantInfoDto participant;

    @Schema(description = "Heure de validation du ticket.")
    private Instant validatedAt;
}
