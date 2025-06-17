package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.structure.*;
import edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureArea;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.AudienceZoneTemplateRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureAreaRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureTypeRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StructureServiceImpl implements StructureService {

    private static final Logger logger = LoggerFactory.getLogger(StructureServiceImpl.class);

    private final StructureRepository structureRepository;
    private final StructureTypeRepository structureTypeRepository;
    private final StructureAreaRepository structureAreaRepository;
    private final AudienceZoneTemplateRepository audienceZoneTemplateRepository;
    private final UserRepository userRepository;

    private final StructureMapper structureMapper;
    private final StructureTypeMapper structureTypeMapper;
    private final AreaMapper areaMapper;
    private final AudienceZoneTemplateMapper audienceZoneTemplateMapper;
    private final AddressMapper addressMapper;


    private final FileStorageService fileStorageService;

    private static final String LOGO_SUBDIR = "structures/logos";
    private static final String COVER_SUBDIR = "structures/covers";
    private static final String GALLERY_SUBDIR = "structures/gallery";

    @Override
    public StructureCreationResponseDto createStructure(StructureCreationDto creationDto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        if (admin.getRole() != edu.cda.project.ticklybackend.enums.UserRole.STRUCTURE_ADMINISTRATOR ||
                Boolean.FALSE.equals(admin.getNeedsStructureSetup())) {
            throw new BadRequestException("L'utilisateur n'est pas autorisé à créer une structure ou en a déjà une.");
        }

        Structure structure = structureMapper.toEntity(creationDto);
        structure.setAdministrator(admin);

        Set<StructureType> types = new HashSet<>(structureTypeRepository.findAllById(creationDto.getTypeIds()));
        if (types.size() != creationDto.getTypeIds().size()) {
            throw new BadRequestException("Un ou plusieurs IDs de type de structure sont invalides.");
        }
        structure.setTypes(types);

        Structure savedStructure = structureRepository.save(structure);

        // Update admin user
        admin.setNeedsStructureSetup(false);
        if (admin instanceof StaffUser) { // Should be StructureAdministratorUser
            ((StaffUser) admin).setStructure(savedStructure);
        }
        userRepository.save(admin);
        logger.info("Structure {} créée par l'administrateur {}", savedStructure.getName(), adminEmail);


        return new StructureCreationResponseDto(
                savedStructure.getId(),
                savedStructure.getName(),
                "Structure créée avec succès.",
                true // needsReAuth
        );
    }

    @Transactional(readOnly = true)
    public Page<StructureSummaryDto> getAllStructures(Pageable pageable, Map<String, String> filters) {
        // Basic implementation without filters for now.
        // Filters can be implemented using JPA Specifications.
        return structureRepository.findAll(pageable)
                .map(structure -> structureMapper.toSummaryDto(structure, fileStorageService));
    }

    @Override
    @Transactional(readOnly = true)
    public StructureDetailResponseDto getStructureById(Long structureId) {
        return structureRepository.findById(structureId)
                .map(structure -> structureMapper.toDetailDto(structure, fileStorageService))
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));
    }

    @Override
    public StructureDetailResponseDto updateStructure(Long structureId, StructureUpdateDto updateDto) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        structureMapper.updateEntityFromDto(updateDto, structure);

        if (updateDto.getTypeIds() != null && !updateDto.getTypeIds().isEmpty()) {
            Set<StructureType> types = new HashSet<>(structureTypeRepository.findAllById(updateDto.getTypeIds()));
            if (types.size() != updateDto.getTypeIds().size()) {
                throw new BadRequestException("Un ou plusieurs IDs de type de structure fournis pour la mise à jour sont invalides.");
            }
            structure.setTypes(types);
        }

        if (updateDto.getAddress() != null) {
            addressMapper.updateAddressFromDto(updateDto.getAddress(), structure.getAddress());
        }


        Structure updatedStructure = structureRepository.save(structure);
        logger.info("Structure {} (ID: {}) mise à jour.", updatedStructure.getName(), structureId);
        return structureMapper.toDetailDto(updatedStructure, fileStorageService);
    }


    @Override
    public void deleteStructure(Long structureId) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        // Clean up associated files before deleting the entity
        try {
            if (StringUtils.hasText(structure.getLogoPath())) {
                fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
            }
            if (StringUtils.hasText(structure.getCoverPath())) {
                fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
            }
            if (structure.getGalleryImagePaths() != null) {
                for (String imagePath : structure.getGalleryImagePaths()) {
                    fileStorageService.deleteFile(imagePath, GALLERY_SUBDIR);
                }
            }
        } catch (FileStorageException e) {
            logger.error("Erreur lors de la suppression des fichiers associés à la structure ID {}: {}", structureId, e.getMessage());
            // Decide if this should be a critical failure or just a warning
        }

        // Unlink from administrator if StaffUser
        User admin = structure.getAdministrator();
        if (admin instanceof StaffUser && ((StaffUser) admin).getStructure() != null && ((StaffUser) admin).getStructure().getId().equals(structureId)) {
            ((StaffUser) admin).setStructure(null);
            // Potentially set needsStructureSetup back to true if business logic dictates
            // admin.setNeedsStructureSetup(true);
            userRepository.save(admin);
        }


        structureRepository.delete(structure);
        logger.info("Structure {} (ID: {}) supprimée.", structure.getName(), structureId);
    }

    @Override
    public FileUploadResponseDto updateStructureLogo(Long structureId, MultipartFile file) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        if (StringUtils.hasText(structure.getLogoPath())) {
            try {
                fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
            } catch (FileStorageException e) {
                logger.warn("Ancien logo non trouvé ou impossible à supprimer pour la structure ID {}: {}", structureId, e.getMessage());
            }
        }

        String newLogoPath = fileStorageService.storeFile(file, LOGO_SUBDIR);
        structure.setLogoPath(newLogoPath);
        structureRepository.save(structure);
        logger.info("Logo mis à jour pour la structure {} (ID: {}). Nouveau chemin: {}", structure.getName(), structureId, newLogoPath);


        return new FileUploadResponseDto(
                file.getOriginalFilename(),
                fileStorageService.getFileUrl(newLogoPath, LOGO_SUBDIR),
                "Logo de la structure mis à jour avec succès."
        );
    }

    @Override
    public FileUploadResponseDto updateStructureCover(Long structureId, MultipartFile file) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        if (StringUtils.hasText(structure.getCoverPath())) {
            try {
                fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
            } catch (FileStorageException e) {
                logger.warn("Ancienne image de couverture non trouvée ou impossible à supprimer pour la structure ID {}: {}", structureId, e.getMessage());
            }
        }

        String newCoverPath = fileStorageService.storeFile(file, COVER_SUBDIR);
        structure.setCoverPath(newCoverPath);
        structureRepository.save(structure);
        logger.info("Image de couverture mise à jour pour la structure {} (ID: {}). Nouveau chemin: {}", structure.getName(), structureId, newCoverPath);

        return new FileUploadResponseDto(
                file.getOriginalFilename(),
                fileStorageService.getFileUrl(newCoverPath, COVER_SUBDIR),
                "Image de couverture de la structure mise à jour avec succès."
        );
    }

    @Override
    public FileUploadResponseDto addStructureGalleryImage(Long structureId, MultipartFile file) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        String newImagePath = fileStorageService.storeFile(file, GALLERY_SUBDIR);
        structure.getGalleryImagePaths().add(newImagePath);
        structureRepository.save(structure);
        logger.info("Image ajoutée à la galerie pour la structure {} (ID: {}). Chemin: {}", structure.getName(), structureId, newImagePath);


        return new FileUploadResponseDto(
                file.getOriginalFilename(),
                fileStorageService.getFileUrl(newImagePath, GALLERY_SUBDIR),
                "Image ajoutée à la galerie de la structure avec succès."
        );
    }

    @Override
    public void removeStructureGalleryImage(Long structureId, String imagePath) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        // imagePath est le nom de fichier unique (ex: uuid.jpg)
        if (!structure.getGalleryImagePaths().contains(imagePath)) {
            throw new ResourceNotFoundException("Image", "path", imagePath + " in structure gallery " + structureId);
        }

        try {
            fileStorageService.deleteFile(imagePath, GALLERY_SUBDIR);
            structure.getGalleryImagePaths().remove(imagePath);
            structureRepository.save(structure);
            logger.info("Image {} supprimée de la galerie pour la structure {} (ID: {}).", imagePath, structure.getName(), structureId);
        } catch (FileStorageException e) {
            logger.error("Impossible de supprimer le fichier image {} de la galerie pour la structure ID {}: {}", imagePath, structureId, e.getMessage());
            throw new BadRequestException("Impossible de supprimer le fichier image de la galerie: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StructureTypeDto> getAllStructureTypes() {
        return structureTypeRepository.findAll().stream()
                .map(structureTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- StructureArea Operations ---
    @Override
    @Transactional(readOnly = true)
    public List<AreaResponseDto> getAreasByStructureId(Long structureId) {
        if (!structureRepository.existsById(structureId)) {
            throw new ResourceNotFoundException("Structure", "id", structureId);
        }
        return structureAreaRepository.findByStructureId(structureId).stream()
                .map(areaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AreaResponseDto getAreaById(Long structureId, Long areaId) {
        return structureAreaRepository.findByIdAndStructureId(areaId, structureId)
                .map(areaMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id " + areaId + " for structure", structureId));
    }


    @Override
    public AreaResponseDto createArea(Long structureId, AreaCreationDto creationDto) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));
        StructureArea area = areaMapper.toEntity(creationDto);
        area.setStructure(structure);
        StructureArea savedArea = structureAreaRepository.save(area);
        logger.info("Espace {} créé pour la structure {} (ID: {}).", savedArea.getName(), structure.getName(), structureId);
        return areaMapper.toDto(savedArea);
    }

    @Override
    public AreaResponseDto updateArea(Long structureId, Long areaId, AreaUpdateDto updateDto) {
        StructureArea area = structureAreaRepository.findByIdAndStructureId(areaId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id " + areaId + " for structure", structureId));
        areaMapper.updateFromDto(updateDto, area);
        StructureArea updatedArea = structureAreaRepository.save(area);
        logger.info("Espace {} (ID: {}) mis à jour pour la structure {} (ID: {}).", updatedArea.getName(), areaId, area.getStructure().getName(), structureId);
        return areaMapper.toDto(updatedArea);
    }

    @Override
    public void deleteArea(Long structureId, Long areaId) {
        StructureArea area = structureAreaRepository.findByIdAndStructureId(areaId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id " + areaId + " for structure", structureId));
        structureAreaRepository.delete(area);
        logger.info("Espace {} (ID: {}) supprimé de la structure {} (ID: {}).", area.getName(), areaId, area.getStructure().getName(), structureId);
    }

    // --- AudienceZoneTemplate Operations ---
    @Override
    @Transactional(readOnly = true)
    public List<AudienceZoneTemplateResponseDto> getAudienceZoneTemplatesByAreaId(Long structureId, Long areaId) {
        if (!structureAreaRepository.existsByIdAndStructureId(areaId, structureId)) {
            throw new ResourceNotFoundException("Area", "id " + areaId + " for structure", structureId);
        }
        return audienceZoneTemplateRepository.findByAreaId(areaId).stream()
                .map(audienceZoneTemplateMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AudienceZoneTemplateResponseDto getAudienceZoneTemplateById(Long structureId, Long areaId, Long templateId) {
        AudienceZoneTemplate template = audienceZoneTemplateRepository.findByIdAndAreaId(templateId, areaId)
                .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id " + templateId + " for area", areaId));
        // Additional check for structure ownership of the area
        if (!template.getArea().getStructure().getId().equals(structureId)) {
            throw new ResourceNotFoundException("AudienceZoneTemplate", "id " + templateId + " for structure", structureId);
        }
        return audienceZoneTemplateMapper.toDto(template);
    }

    @Override
    public AudienceZoneTemplateResponseDto createAudienceZoneTemplate(Long structureId, Long areaId, AudienceZoneTemplateCreationDto creationDto) {
        StructureArea area = structureAreaRepository.findByIdAndStructureId(areaId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id " + areaId + " for structure", structureId));
        AudienceZoneTemplate template = audienceZoneTemplateMapper.toEntity(creationDto);
        template.setArea(area);
        AudienceZoneTemplate savedTemplate = audienceZoneTemplateRepository.save(template);
        logger.info("Modèle de zone {} créé pour l'espace {} (ID: {}).", savedTemplate.getName(), area.getName(), areaId);
        return audienceZoneTemplateMapper.toDto(savedTemplate);
    }

    @Override
    public AudienceZoneTemplateResponseDto updateAudienceZoneTemplate(Long structureId, Long areaId, Long templateId, AudienceZoneTemplateUpdateDto updateDto) {
        AudienceZoneTemplate template = audienceZoneTemplateRepository.findByIdAndAreaId(templateId, areaId)
                .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id " + templateId + " for area", areaId));
        if (!template.getArea().getStructure().getId().equals(structureId)) { // Ensure area belongs to structure
            throw new ResourceNotFoundException("AudienceZoneTemplate", "id " + templateId + " for structure", structureId);
        }
        audienceZoneTemplateMapper.updateFromDto(updateDto, template);
        AudienceZoneTemplate updatedTemplate = audienceZoneTemplateRepository.save(template);
        logger.info("Modèle de zone {} (ID: {}) mis à jour pour l'espace {} (ID: {}).", updatedTemplate.getName(), templateId, template.getArea().getName(), areaId);
        return audienceZoneTemplateMapper.toDto(updatedTemplate);
    }

    @Override
    public void deleteAudienceZoneTemplate(Long structureId, Long areaId, Long templateId) {
        AudienceZoneTemplate template = audienceZoneTemplateRepository.findByIdAndAreaId(templateId, areaId)
                .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id " + templateId + " for area", areaId));
        if (!template.getArea().getStructure().getId().equals(structureId)) { // Ensure area belongs to structure
            throw new ResourceNotFoundException("AudienceZoneTemplate", "id " + templateId + " for structure", structureId);
        }
        audienceZoneTemplateRepository.delete(template);
        logger.info("Modèle de zone {} (ID: {}) supprimé de l'espace {} (ID: {}).", template.getName(), templateId, template.getArea().getName(), areaId);
    }
}