package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.structure.StructureTypeDto;

import java.util.List;

public interface StructureTypeService {
    List<StructureTypeDto> getAllStructureTypes();
}