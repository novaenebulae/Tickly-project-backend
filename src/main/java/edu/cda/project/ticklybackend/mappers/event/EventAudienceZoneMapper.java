package edu.cda.project.ticklybackend.mappers.event;

import edu.cda.project.ticklybackend.dtos.event.EventAudienceZoneDto;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventAudienceZoneMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "allocatedCapacity", target = "allocatedCapacity"),
            @Mapping(source = "template.id", target = "templateId"),
            @Mapping(source = "template.name", target = "name"),
            @Mapping(source = "template.seatingType", target = "seatingType"),
            @Mapping(source = "template.active", target = "isActive"),
            @Mapping(source = "template.area.id", target = "areaId")
    })
    EventAudienceZoneDto toDto(EventAudienceZone entity);

    List<EventAudienceZoneDto> toDtoList(List<EventAudienceZone> entityList);
}
