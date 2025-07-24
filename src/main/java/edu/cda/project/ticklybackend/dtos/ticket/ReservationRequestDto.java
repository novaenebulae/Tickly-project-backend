package edu.cda.project.ticklybackend.dtos.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Requête pour créer une nouvelle réservation pour un ou plusieurs billets.")
public class ReservationRequestDto {

    @NotNull(message = "L'ID de l'événement est requis.")
    @Schema(description = "ID de l'événement pour lequel les billets sont réservés.", example = "1")
    private Long eventId;

    @NotNull(message = "L'ID de la zone d'audience est requis.")
    @Schema(description = "ID de la zone d'audience spécifique au sein de l'événement.", example = "1")
    private Long audienceZoneId;

    @Valid
    @NotNull
    @Size(min = 1, max = 4, message = "Vous pouvez réserver entre 1 et 4 billets à la fois.")
    @Schema(description = "Liste des participants pour lesquels les billets sont réservés.")
    private List<ParticipantInfoDto> participants;
}
