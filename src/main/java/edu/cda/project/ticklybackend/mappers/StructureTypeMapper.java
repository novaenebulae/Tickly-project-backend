package edu.cda.project.ticklybackend.mappers;

import edu.cda.project.ticklybackend.dtos.structure.StructureTypeDto;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StructureTypeMapper {
    StructureTypeDto structureTypeToStructureTypeDto(StructureType structureType);

    List<StructureTypeDto> structureTypesToStructureTypeDtos(List<StructureType> structureTypes);
}