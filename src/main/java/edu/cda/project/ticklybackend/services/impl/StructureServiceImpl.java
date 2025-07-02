package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.structure.*;
import edu.cda.project.ticklybackend.models.structure.*;
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.structure.AudienceZoneTemplateRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureAreaRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureTypeRepository;
import edu.cda.project.ticklybackend.repositories.user.UserFavoriteStructureRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StructureServiceImpl implements StructureService {

    private static final Logger logger = LoggerFactory.getLogger(StructureServiceImpl.class);

    private final StructureRepository structureRepository;
    private final StructureTypeRepository structureTypeRepository;
    private final StructureAreaRepository structureAreaRepository;
    private final AudienceZoneTemplateRepository audienceZoneTemplateRepository;
    private final UserRepository userRepository;
    private final MailingService mailingService; // Injection pour la notification
    private final EventRepository eventRepository; // Injection pour la vérification
    private final UserFavoriteStructureRepository favoriteRepository;

    private final StructureMapper structureMapper;
    private final StructureTypeMapper structureTypeMapper;
    private final AreaMapper areaMapper;
    private final AudienceZoneTemplateMapper audienceZoneTemplateMapper;
    private final AddressMapper addressMapper;

    private final TeamManagementService teamService;
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
    public Page<StructureSummaryDto> getAllStructures(Pageable pageable, StructureSearchParamsDto params) {

        Specification<Structure> spec = StructureSpecification.getSpecification(params);
        return structureRepository.findAll(spec, pageable)
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
    @Transactional
    public void deleteStructure(Long structureId) {
        log.warn("Début de la tentative de suppression de la structure ID: {}", structureId);
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        // GARDE-FOU: Interdire la suppression si des événements sont encore actifs ou en brouillon.
        if (eventRepository.existsByStructureIdAndStatusIn(structureId, Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT))) {
            throw new BadRequestException("Suppression impossible : Des événements actifs ou en brouillon existent pour cette structure. " +
                    "Pour supprimer la structure, vous devez d'abord :\n" +
                    "1. Accéder à la liste des événements de votre structure\n" +
                    "2. Annuler tous les événements actifs (statut PUBLISHED)\n" +
                    "3. Supprimer tous les brouillons (statut DRAFT)\n" +
                    "Une fois tous les événements traités, vous pourrez supprimer la structure.");
        }

        // Sauvegarde des informations pour la notification avant l'anonymisation
        User admin = structure.getAdministrator();
        String originalStructureName = structure.getName();

        // 1. Dissolution des relations
        log.info("Suppression des favoris pour la structure ID: {}", structureId);
        favoriteRepository.deleteByStructureId(structureId);

        // Dissolution de l'équipe - conversion des membres en SPECTATOR
        log.info("Dissolution de l'équipe pour la structure ID: {}", structureId);
        teamService.dissolveTeam(structureId);

        // 2. Nettoyage des fichiers physiques
        log.info("Nettoyage des fichiers pour la structure ID: {}", structureId);
        if (StringUtils.hasText(structure.getLogoPath()))
            fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
        if (StringUtils.hasText(structure.getCoverPath()))
            fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
        if (structure.getGalleryImagePaths() != null) {
            structure.getGalleryImagePaths().forEach(path -> fileStorageService.deleteFile(path, GALLERY_SUBDIR));
        }

        // 3. Anonymisation de l'entité Structure (Soft Delete)
        log.info("Anonymisation de la structure ID: {}", structureId);
        structure.setName("Structure supprimée (" + structure.getId() + ")");
        structure.setDescription("Cette structure a été supprimée le " + Instant.now());
        structure.setAddress(new StructureAddress("Rue supprimée", "Ville supprimée", "0", "Pays supprimé"));
        structure.setPhone(null);
        structure.setEmail("anonymized+" + structure.getId() + "@tickly.app");
        structure.setWebsiteUrl(null);
        structure.setLogoPath(null);
        structure.setCoverPath(null);
        structure.getGalleryImagePaths().clear();
        structure.getSocialMediaLinks().clear();
        structure.setActive(false);
        structure.setAdministrator(null); // Rompre le lien avec l'admin

        structureRepository.save(structure);
        log.info("Anonymisation de la structure ID: {} terminée.", structureId);

        // 4. Dissociation de l'administrateur
        if (admin instanceof StaffUser) {
            log.info("Dissociation de l'administrateur ID: {} de la structure supprimée.", admin.getId());
            ((StaffUser) admin).setStructure(null);
            admin.setNeedsStructureSetup(true);
            userRepository.save(admin);
        }

        // 5. Communication finale
        if (admin != null) {
            mailingService.sendStructureDeletionConfirmation(admin.getEmail(), admin.getFirstName(), originalStructureName);
        }

        log.warn("Suppression de la structure ID: {} terminée avec succès.", structureId);
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
    public void removeStructureLogo(Long structureId) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        if (!StringUtils.hasText(structure.getLogoPath())) {
            throw new ResourceNotFoundException("Logo", "structureId", structureId);
        }

        try {
            fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
            structure.setLogoPath(null);
            structureRepository.save(structure);
            logger.info("Logo supprimé avec succès pour la structure {} (ID: {}).", structure.getName(), structureId);
        } catch (FileStorageException e) {
            logger.error("Impossible de supprimer le fichier logo pour la structure ID {}: {}", structureId, e.getMessage());
            throw new BadRequestException("Impossible de supprimer le fichier logo: " + e.getMessage());
        }
    }

    @Override
    public void removeStructureCover(Long structureId) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        if (!StringUtils.hasText(structure.getCoverPath())) {
            throw new ResourceNotFoundException("Image de couverture", "structureId", structureId);
        }

        try {
            fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
            structure.setCoverPath(null);
            structureRepository.save(structure);
            logger.info("Image de couverture supprimée avec succès pour la structure {} (ID: {}).", structure.getName(), structureId);
        } catch (FileStorageException e) {
            logger.error("Impossible de supprimer le fichier image de couverture pour la structure ID {}: {}", structureId, e.getMessage());
            throw new BadRequestException("Impossible de supprimer le fichier image de couverture: " + e.getMessage());
        }
    }

//    @Override
//    public FileUploadResponseDto addStructureGalleryImage(Long structureId, MultipartFile file) {
//        Structure structure = structureRepository.findById(structureId)
//                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));
//
//        String newImagePath = fileStorageService.storeFile(file, GALLERY_SUBDIR);
//        structure.getGalleryImagePaths().add(newImagePath);
//        structureRepository.save(structure);
//        logger.info("Image ajoutée à la galerie pour la structure {} (ID: {}). Chemin: {}", structure.getName(), structureId, newImagePath);
//
//
//        return new FileUploadResponseDto(
//                file.getOriginalFilename(),
//                fileStorageService.getFileUrl(newImagePath, GALLERY_SUBDIR),
//                "Image ajoutée à la galerie de la structure avec succès."
//        );
//    }

    @Override
    @Transactional
    public List<FileUploadResponseDto> addStructureGalleryImages(Long structureId, MultipartFile[] files) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        List<FileUploadResponseDto> results = new ArrayList<>();
        List<String> successfulUploads = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Validation du fichier
                if (file.isEmpty()) {
                    results.add(new FileUploadResponseDto(
                            file.getOriginalFilename(),
                            null,
                            "Erreur: fichier vide"));
                    continue;
                }

                // Stocker le fichier
                String filename = fileStorageService.storeFile(file, GALLERY_SUBDIR);
                String fileUrl = fileStorageService.getFileUrl(filename, GALLERY_SUBDIR);

                // Ajouter à la liste des uploads réussis
                successfulUploads.add(filename);

                results.add(new FileUploadResponseDto(
                        file.getOriginalFilename(),
                        fileUrl,
                        "Image ajoutée avec succès"));

            } catch (Exception e) {
                logger.error("Erreur lors de l'upload du fichier {}: {}",
                        file.getOriginalFilename(), e.getMessage());
                results.add(new FileUploadResponseDto(
                        file.getOriginalFilename(),
                        null,
                        "Erreur: " + e.getMessage()));
            }
        }

        // Ajouter tous les fichiers uploadés avec succès à la galerie
        if (!successfulUploads.isEmpty()) {
            structure.getGalleryImagePaths().addAll(successfulUploads);
            structureRepository.save(structure);
            logger.info("Ajout de {} images à la galerie de la structure {}",
                    successfulUploads.size(), structureId);
        }

        return results;
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
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", areaId));

        // GARDE-FOU : Si l'area est utilisée par des événements actifs, limiter les modifications
        Set<EventStatus> activeStatuses = Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT);
        boolean isUsedByActiveEvents = eventRepository.existsByAreaIdAndStatusIn(areaId, activeStatuses);

        if (isUsedByActiveEvents) {
            // Seuls le nom et la description peuvent être modifiés
            if (updateDto.getMaxCapacity() != null || updateDto.getIsActive() != null) {
                throw new BadRequestException(
                        "Modification restreinte : Cette area ('" + area.getName() + "') est utilisée par des événements actifs. " +
                                "Seuls le nom et la description peuvent être modifiés. " +
                                "Capacité maximale et statut d'activation sont protégés."
                );
            }
        }

        // Appliquer les modifications autorisées
        if (StringUtils.hasText(updateDto.getName())) {
            area.setName(updateDto.getName());
        }
        if (StringUtils.hasText(updateDto.getDescription())) {
            area.setDescription(updateDto.getDescription());
        }

        // Modifications conditionnelles (seulement si pas d'événements actifs)
        if (!isUsedByActiveEvents) {
            if (updateDto.getMaxCapacity() != null) {
                area.setMaxCapacity(updateDto.getMaxCapacity());
            }
            if (updateDto.getIsActive() != null) {
                area.setActive(updateDto.getIsActive());
            }
        }

        StructureArea savedArea = structureAreaRepository.save(area);
        log.info("Area '{}' (ID: {}) mise à jour pour la structure ID: {}", savedArea.getName(), areaId, structureId);
        return areaMapper.toDto(savedArea);
    }

    @Override
    public void deleteArea(Long structureId, Long areaId) {
        // Vérifier que l'area appartient à la structure
        StructureArea area = structureAreaRepository.findByIdAndStructureId(areaId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", areaId));

        // GARDE-FOU : Interdire la suppression si l'area est utilisée par des événements actifs
        Set<EventStatus> activeStatuses = Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT);
        if (eventRepository.existsByAreaIdAndStatusIn(areaId, activeStatuses)) {
            throw new BadRequestException(
                    "Suppression impossible : Cette area ('" + area.getName() + "') est actuellement utilisée par des événements actifs ou en brouillon. " +
                            "Veuillez d'abord annuler ou terminer ces événements."
            );
        }

        log.info("Suppression de l'area '{}' (ID: {}) pour la structure ID: {}", area.getName(), areaId, structureId);
        structureAreaRepository.delete(area);
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
        AudienceZoneTemplate template = audienceZoneTemplateRepository.findByIdAndAreaIdAndAreaStructureId(templateId, areaId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id", templateId));

        // GARDE-FOU : Si le template est utilisé par des événements actifs, limiter les modifications
        Set<EventStatus> activeStatuses = Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT);
        boolean isUsedByActiveEvents = eventRepository.existsByTemplateIdAndStatusIn(templateId, activeStatuses);

        if (isUsedByActiveEvents) {
            // Seuls le nom et éventuellement la description peuvent être modifiés
            if (updateDto.getMaxCapacity() != null || updateDto.getSeatingType() != null || updateDto.getIsActive() != null) {
                throw new BadRequestException(
                        "Modification restreinte : Cette zone d'audience ('" + template.getName() + "') est utilisée par des événements actifs. " +
                                "Seuls le nom peut être modifié. " +
                                "Capacité, type de placement et statut d'activation sont protégés."
                );
            }
        }

        // Appliquer les modifications autorisées
        if (StringUtils.hasText(updateDto.getName())) {
            template.setName(updateDto.getName());
        }

        // Modifications conditionnelles (seulement si pas d'événements actifs)
        if (!isUsedByActiveEvents) {
            if (updateDto.getMaxCapacity() != null) {
                template.setMaxCapacity(updateDto.getMaxCapacity());
            }
            if (updateDto.getSeatingType() != null) {
                template.setSeatingType(updateDto.getSeatingType());
            }
            if (updateDto.getIsActive() != null) {
                template.setActive(updateDto.getIsActive());
            }
        }

        AudienceZoneTemplate savedTemplate = audienceZoneTemplateRepository.save(template);
        log.info("Template de zone '{}' (ID: {}) mis à jour pour l'area ID: {}", savedTemplate.getName(), templateId, areaId);
        return audienceZoneTemplateMapper.toDto(savedTemplate);
    }

    @Override
    public void deleteAudienceZoneTemplate(Long structureId, Long areaId, Long templateId) {
        AudienceZoneTemplate template = audienceZoneTemplateRepository.findByIdAndAreaIdAndAreaStructureId(templateId, areaId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id", templateId));

        // GARDE-FOU : Interdire la suppression si le template est utilisé par des événements actifs
        Set<EventStatus> activeStatuses = Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT);
        if (eventRepository.existsByTemplateIdAndStatusIn(templateId, activeStatuses)) {
            throw new BadRequestException(
                    "Suppression impossible : Cette zone d'audience ('" + template.getName() + "') est actuellement utilisée par des événements actifs ou en brouillon. " +
                            "Veuillez d'abord annuler ou terminer ces événements."
            );
        }

        log.info("Suppression du template de zone '{}' (ID: {}) pour l'area ID: {}", template.getName(), templateId, areaId);
        audienceZoneTemplateRepository.delete(template);
    }
}
