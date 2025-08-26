package edu.cda.project.ticklybackend.dtos.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request to create a reservation for one or more tickets.")
public class ReservationRequestDto {

    @NotNull(message = "L'ID de l'événement est requis.")
    @Schema(description = "Event ID for which tickets are reserved.", example = "1")
    private Long eventId;

    @NotNull(message = "L'ID de la zone d'audience est requis.")
    @Schema(description = "Audience zone ID within the event.", example = "1")
    private Long audienceZoneId;

    @Valid
    @NotNull
    @Size(min = 1, max = 4, message = "Vous pouvez réserver entre 1 et 4 billets à la fois.")
    @Schema(description = "List of participants for whom tickets are reserved.")
    private List<ParticipantInfoDto> participants;
}
