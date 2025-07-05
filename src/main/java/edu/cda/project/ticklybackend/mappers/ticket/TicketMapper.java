package edu.cda.project.ticklybackend.mappers.ticket;

import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);

    @Mapping(source = "event.id", target = "eventSnapshot.eventId")
    @Mapping(source = "event.name", target = "eventSnapshot.name")
    @Mapping(source = "event.startDate", target = "eventSnapshot.startDate")
    @Mapping(source = "event.address", target = "eventSnapshot.address")
    @Mapping(source = "event.mainPhotoPath", target = "eventSnapshot.mainPhotoUrl")
    @Mapping(source = "eventAudienceZone.id", target = "audienceZoneSnapshot.audienceZoneId")

    @Mapping(source = "eventAudienceZone.template.name", target = "audienceZoneSnapshot.name")
    @Mapping(source = "eventAudienceZone.template.seatingType", target = "audienceZoneSnapshot.seatingType")

    @Mapping(source = "participantFirstName", target = "participant.firstName")
    @Mapping(source = "participantLastName", target = "participant.lastName")
    @Mapping(source = "participantEmail", target = "participant.email")
    TicketResponseDto toDto(Ticket ticket);

    List<TicketResponseDto> toDtoList(List<Ticket> tickets);

    default ZonedDateTime toZonedDateTime(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
