package edu.cda.project.ticklybackend.mappers.event;

import edu.cda.project.ticklybackend.dtos.event.EventAudienceZoneDto;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventAudienceZoneMapper {

    @Mapping(target = "event", ignore = true)
    EventAudienceZone toEntity(EventAudienceZoneDto dto);

    EventAudienceZoneDto toDto(EventAudienceZone entity);

    List<EventAudienceZone> toEntityList(List<EventAudienceZoneDto> dtoList);

    List<EventAudienceZoneDto> toDtoList(List<EventAudienceZone> entityList);
}