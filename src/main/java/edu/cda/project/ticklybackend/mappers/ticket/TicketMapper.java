package edu.cda.project.ticklybackend.mappers.ticket;

import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Mapper for the ticket entity and its DTOs.
 */
@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);

    @Mapping(source = "event.id", target = "eventSnapshot.eventId")
    @Mapping(source = "event.name", target = "eventSnapshot.name")
    @Mapping(source = "event.startDate", target = "eventSnapshot.startDate")
    @Mapping(source = "event.address", target = "eventSnapshot.address")
    @Mapping(source = "event.mainPhotoPath", target = "eventSnapshot.mainPhotoUrl")
    // Note: this will need post-processing to build full URL
    @Mapping(source = "eventAudienceZone.id", target = "audienceZoneSnapshot.audienceZoneId")
    @Mapping(source = "eventAudienceZone.name", target = "audienceZoneSnapshot.name")
    @Mapping(source = "eventAudienceZone.seatingType", target = "audienceZoneSnapshot.seatingType")
    @Mapping(source = "participantFirstName", target = "participant.firstName")
    @Mapping(source = "participantLastName", target = "participant.lastName")
    @Mapping(source = "participantEmail", target = "participant.email")
    TicketResponseDto toDto(Ticket ticket);

    List<TicketResponseDto> toDtoList(List<Ticket> tickets);

    /**
     * Convertit un Instant en LocalDateTime.
     * Utilise le fuseau horaire système par défaut.
     * Appelée automatiquement par MapStruct lors du mapping.
     */
    default LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}