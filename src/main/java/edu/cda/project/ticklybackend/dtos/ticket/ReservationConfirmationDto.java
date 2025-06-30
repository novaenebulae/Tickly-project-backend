package edu.cda.project.ticklybackend.dtos.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Réponse de confirmation après une réservation réussie.")
public class ReservationConfirmationDto {

    @Schema(description = "ID de la réservation qui groupe les billets.", example = "101")
    private Long reservationId;

    @Schema(description = "Liste de tous les billets créés dans cette réservation.")
    private List<TicketResponseDto> tickets;

    @Schema(description = "Date et heure de la réservation.")
    private LocalDateTime reservationDate;
}
