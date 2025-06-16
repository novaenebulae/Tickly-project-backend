package edu.cda.project.ticklybackend.mappers.structure;

import edu.cda.project.ticklybackend.dtos.structure.AreaCreationDto;
import edu.cda.project.ticklybackend.dtos.structure.AreaResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.AreaUpdateDto;
import edu.cda.project.ticklybackend.models.structure.StructureArea;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AudienceZoneTemplateMapper.class})
public interface AreaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "structure", ignore = true) // sera setté manuellement
    @Mapping(target = "audienceZoneTemplates", ignore = true)
        // géré séparément
    StructureArea toEntity(AreaCreationDto dto);

    @Mapping(source = "structure.id", target = "structureId")
    @Mapping(source = "audienceZoneTemplates", target = "audienceZoneTemplates")
    AreaResponseDto toDto(StructureArea entity);

    List<AreaResponseDto> toDtoList(List<StructureArea> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "structure", ignore = true)
    @Mapping(target = "audienceZoneTemplates", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AreaUpdateDto dto, @MappingTarget StructureArea entity);
}