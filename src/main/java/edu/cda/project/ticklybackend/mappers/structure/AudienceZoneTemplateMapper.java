package edu.cda.project.ticklybackend.mappers.structure;

import edu.cda.project.ticklybackend.dtos.structure.AudienceZoneTemplateCreationDto;
import edu.cda.project.ticklybackend.dtos.structure.AudienceZoneTemplateResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.AudienceZoneTemplateUpdateDto;
import edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AudienceZoneTemplateMapper {

    @Mapping(target = "id", ignore = true) // id est auto-généré
    @Mapping(target = "area", ignore = true)
    AudienceZoneTemplate toEntity(AudienceZoneTemplateCreationDto dto);

    @Mapping(source = "area.id", target = "areaId")
    AudienceZoneTemplateResponseDto toDto(AudienceZoneTemplate entity);

    List<AudienceZoneTemplateResponseDto> toDtoList(List<AudienceZoneTemplate> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "area", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AudienceZoneTemplateUpdateDto dto, @MappingTarget AudienceZoneTemplate entity);
}