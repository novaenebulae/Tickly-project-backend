package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.structure.StructureTypeDto;
import edu.cda.project.ticklybackend.mappers.StructureTypeMapper;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.repositories.StructureTypeRepository;
import edu.cda.project.ticklybackend.services.interfaces.StructureTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StructureTypeServiceImpl implements StructureTypeService {

    private final StructureTypeRepository structureTypeRepository;
    private final StructureTypeMapper structureTypeMapper;

    @Override
    public List<StructureTypeDto> getAllStructureTypes() {
        List<StructureType> types = structureTypeRepository.findAll();
        return structureTypeMapper.structureTypesToStructureTypeDtos(types);
    }
}