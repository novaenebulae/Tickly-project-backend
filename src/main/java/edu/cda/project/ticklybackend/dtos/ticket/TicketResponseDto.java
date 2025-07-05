package edu.cda.project.ticklybackend.dtos.ticket;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.enums.SeatingType;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Schema(description = "Réponse détaillée pour un billet unique.")
public class TicketResponseDto {

    @Schema(description = "ID unique du billet.", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Valeur à encoder dans le QR code.", example = "qr-code-unique-value-string")
    private String qrCodeValue;

    @Schema(description = "Statut actuel du billet.")
    private TicketStatus status;

    @Schema(description = "Informations sur le détenteur du billet.")
    private ParticipantInfoDto participant;

    @Schema(description = "Instantané des détails de l'événement au moment de l'achat.")
    private EventTicketSnapshotDto eventSnapshot;

    @Schema(description = "Instantané des détails de la zone d'audience pour ce billet.")
    private AudienceZoneTicketSnapshotDto audienceZoneSnapshot;

    @Schema(description = "Date et heure de la réservation.", example = "2025-06-18T10:00:00")
    private ZonedDateTime reservation_date_time;

//    @Schema(description = "Prix du billet.", example = "45.50")
//    private BigDecimal price;

//    @Schema(description = "Numéro de siège assigné, le cas échéant.", example = "A-12")
//    private String seatNumber;

    @Data
    @Schema(name = "EventTicketSnapshot", description = "Instantané des informations clés de l'événement.")
    public static class EventTicketSnapshotDto {
        private Long eventId;
        private String name;
        private ZonedDateTime startDate;
        private AddressDto address;
        private String mainPhotoUrl;
    }

    @Data
    @Schema(name = "AudienceZoneTicketSnapshot", description = "Instantané des informations clés de la zone d'audience.")
    public static class AudienceZoneTicketSnapshotDto {
        private Long audienceZoneId;
        private String name;
        private SeatingType seatingType;
    }
}
