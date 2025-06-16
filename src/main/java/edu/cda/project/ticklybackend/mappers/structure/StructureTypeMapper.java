package edu.cda.project.ticklybackend.mappers.structure;

import edu.cda.project.ticklybackend.dtos.structure.StructureTypeDto;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface StructureTypeMapper {

    StructureTypeDto toDto(StructureType entity);

    StructureType toEntity(StructureTypeDto dto);

    List<StructureTypeDto> toDtoList(List<StructureType> entities);

    Set<StructureTypeDto> toDtoSet(Set<StructureType> entities);
}