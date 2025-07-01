package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StructureService {

    // Structure Operations
    StructureCreationResponseDto createStructure(StructureCreationDto creationDto, String adminEmail);

    Page<StructureSummaryDto> getAllStructures(Pageable pageable, StructureSearchParamsDto params);

    StructureDetailResponseDto getStructureById(Long structureId);

    StructureDetailResponseDto updateStructure(Long structureId, StructureUpdateDto updateDto);

    void deleteStructure(Long structureId);

    // Structure File Operations
    FileUploadResponseDto updateStructureLogo(Long structureId, MultipartFile file);

    void removeStructureLogo(Long structureId);

    FileUploadResponseDto updateStructureCover(Long structureId, MultipartFile file);

    void removeStructureCover(Long structureId);

//    FileUploadResponseDto addStructureGalleryImage(Long structureId, MultipartFile file);

    List<FileUploadResponseDto> addStructureGalleryImages(Long structureId, MultipartFile[] files);

    void removeStructureGalleryImage(Long structureId, String imagePath);


    // StructureType Operations
    List<StructureTypeDto> getAllStructureTypes();

    // StructureArea Operations
    List<AreaResponseDto> getAreasByStructureId(Long structureId);

    AreaResponseDto getAreaById(Long structureId, Long areaId);

    AreaResponseDto createArea(Long structureId, AreaCreationDto creationDto);

    AreaResponseDto updateArea(Long structureId, Long areaId, AreaUpdateDto updateDto);

    void deleteArea(Long structureId, Long areaId);

    // AudienceZoneTemplate Operations
    List<AudienceZoneTemplateResponseDto> getAudienceZoneTemplatesByAreaId(Long structureId, Long areaId);

    AudienceZoneTemplateResponseDto getAudienceZoneTemplateById(Long structureId, Long areaId, Long templateId);

    AudienceZoneTemplateResponseDto createAudienceZoneTemplate(Long structureId, Long areaId, AudienceZoneTemplateCreationDto creationDto);

    AudienceZoneTemplateResponseDto updateAudienceZoneTemplate(Long structureId, Long areaId, Long templateId, AudienceZoneTemplateUpdateDto updateDto);

    void deleteAudienceZoneTemplate(Long structureId, Long areaId, Long templateId);
}