package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.exceptions.StructureCreationForbiddenException;
import edu.cda.project.ticklybackend.mappers.structure.*;
import edu.cda.project.ticklybackend.models.structure.*;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.structure.AudienceZoneTemplateRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureAreaRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureTypeRepository;
import edu.cda.project.ticklybackend.repositories.user.UserFavoriteStructureRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    private final JwtTokenProvider jwtTokenProvider;

    private final StructureRepository structureRepository;
    private final StructureTypeRepository structureTypeRepository;
    private final StructureAreaRepository structureAreaRepository;
    private final AudienceZoneTemplateRepository audienceZoneTemplateRepository;
    private final UserRepository userRepository;
    private final MailingService mailingService; // Injection pour la notification
    private final EventRepository eventRepository; // Injection pour la vérification
    private final UserFavoriteStructureRepository favoriteRepository;
    private final edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository teamMemberRepository;

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
    public StructureCreationResponseDto createStructure(StructureCreationDto creationDto, String userEmail) {
        LoggingUtils.logMethodEntry(log, "createStructure", "creationDto", creationDto, "userEmail", userEmail);

        try {
            // 1. Trouver l'utilisateur qui crée la structure.
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", userEmail));

            LoggingUtils.setUserId(user.getId());
            log.debug("Utilisateur trouvé: {} (ID: {})", user.getEmail(), user.getId());

            // 2. Vérifier si l'utilisateur est autorisé à créer une structure (nouveau modèle basé sur les memberships).
            if (!user.isEmailValidated()) {
                log.warn("Tentative de création de structure par un utilisateur avec email non validé: {} (ID: {})", user.getEmail(), user.getId());
                throw new StructureCreationForbiddenException("Veuillez valider votre email avant de créer une structure.");
            }
            var existingActiveMembership = teamMemberRepository.findFirstByUserIdAndStatusOrderByJoinedAtDesc(user.getId(), edu.cda.project.ticklybackend.enums.TeamMemberStatus.ACTIVE);
            if (existingActiveMembership.isPresent()) {
                log.warn("Utilisateur {} (ID: {}) déjà membre actif d'une structure, création interdite", user.getEmail(), user.getId());
                throw new StructureCreationForbiddenException("Vous êtes déjà membre actif d'une structure.");
            }

            // 3. Créer et sauvegarder la nouvelle structure.
            Structure structure = structureMapper.toEntity(creationDto);
            // Ici, vous pouvez ajouter une logique pour l'adresse, etc.
            Structure savedStructure = structureRepository.save(structure);
            log.info("Nouvelle structure créée: {} (ID: {})", savedStructure.getName(), savedStructure.getId());

            // 4. Créer l'appartenance TeamMember (ADMIN actif) pour le créateur.
            var newMember = new edu.cda.project.ticklybackend.models.team.TeamMember();
            newMember.setStructure(savedStructure);
            newMember.setUser(user);
            newMember.setEmail(user.getEmail());
            newMember.setRole(edu.cda.project.ticklybackend.enums.UserRole.STRUCTURE_ADMINISTRATOR);
            newMember.setStatus(edu.cda.project.ticklybackend.enums.TeamMemberStatus.ACTIVE);
            newMember.setJoinedAt(Instant.now());
            teamMemberRepository.save(newMember);
            log.info("Membre administrateur créé pour la structure {} (ID: {})", savedStructure.getName(), savedStructure.getId());

            // 5. Générer un nouveau token JWT identité-seulement
            String newJwt = jwtTokenProvider.generateAccessToken(user);
            log.debug("Nouveau token JWT généré pour l'utilisateur: {} (ID: {})", user.getEmail(), user.getId());

            // 6. Préparer la réponse DTO avec le nouveau token.
            StructureCreationResponseDto responseDto = new StructureCreationResponseDto();
            responseDto.setStructureId(savedStructure.getId());
            responseDto.setMessage("Structure créée avec succès. Vous avez été ajouté en tant qu'Administrateur de Structure.");
            responseDto.setAccessToken(newJwt);
            responseDto.setExpiresIn(jwtTokenProvider.getExpirationInMillis());

            LoggingUtils.logMethodExit(log, "createStructure", responseDto);
            return responseDto;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la création de la structure", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StructureSummaryDto> getAllStructures(Pageable pageable, StructureSearchParamsDto params) {
        LoggingUtils.logMethodEntry(log, "getAllStructures", "pageable", pageable, "params", params);

        try {
            log.debug("Recherche de structures avec les paramètres: {}", params);
            Specification<Structure> spec = StructureSpecification.getSpecification(params);
            Page<StructureSummaryDto> result = structureRepository.findAll(spec, pageable)
                    .map(structure -> structureMapper.toSummaryDto(structure, fileStorageService));

            log.debug("Trouvé {} structures sur {} pages", result.getNumberOfElements(), result.getTotalPages());
            LoggingUtils.logMethodExit(log, "getAllStructures", "Page with " + result.getNumberOfElements() + " elements");
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des structures", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StructureDetailResponseDto getStructureById(Long structureId) {
        LoggingUtils.logMethodEntry(log, "getStructureById", "structureId", structureId);

        try {
            log.debug("Recherche de la structure avec ID: {}", structureId);
            StructureDetailResponseDto result = structureRepository.findById(structureId)
                    .map(structure -> {
                        log.debug("Structure trouvée: {} (ID: {})", structure.getName(), structure.getId());
                        return structureMapper.toDetailDto(structure, fileStorageService);
                    })
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            LoggingUtils.logMethodExit(log, "getStructureById", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération de la structure avec ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public StructureDetailResponseDto updateStructure(Long structureId, StructureUpdateDto updateDto) {
        LoggingUtils.logMethodEntry(log, "updateStructure", "structureId", structureId, "updateDto", updateDto);

        try {
            log.debug("Recherche de la structure à mettre à jour avec ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            log.debug("Structure trouvée: {} (ID: {}). Mise à jour en cours...", structure.getName(), structure.getId());
            structureMapper.updateEntityFromDto(updateDto, structure);

            if (updateDto.getTypeIds() != null && !updateDto.getTypeIds().isEmpty()) {
                log.debug("Mise à jour des types de structure: {}", updateDto.getTypeIds());
                Set<StructureType> types = new HashSet<>(structureTypeRepository.findAllById(updateDto.getTypeIds()));
                if (types.size() != updateDto.getTypeIds().size()) {
                    log.warn("IDs de type de structure invalides fournis: {}", updateDto.getTypeIds());
                    throw new BadRequestException("Un ou plusieurs IDs de type de structure fournis pour la mise à jour sont invalides.");
                }
                structure.setTypes(types);
            }

            if (updateDto.getAddress() != null) {
                log.debug("Mise à jour de l'adresse de la structure");
                addressMapper.updateAddressFromDto(updateDto.getAddress(), structure.getAddress());
            }

            Structure updatedStructure = structureRepository.save(structure);
            log.info("Structure {} (ID: {}) mise à jour avec succès.", updatedStructure.getName(), structureId);

            StructureDetailResponseDto result = structureMapper.toDetailDto(updatedStructure, fileStorageService);
            LoggingUtils.logMethodExit(log, "updateStructure", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la mise à jour de la structure avec ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional
    public void deleteStructure(Long structureId) {
        LoggingUtils.logMethodEntry(log, "deleteStructure", "structureId", structureId);

        try {
            log.warn("Début de la tentative de suppression de la structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            // GARDE-FOU: Interdire la suppression si des événements sont encore actifs.
            if (eventRepository.existsByStructureIdAndStatusIn(structureId, Set.of(EventStatus.PUBLISHED))) {
                log.warn("Tentative de suppression d'une structure avec des événements actifs. Structure ID: {}", structureId);
                throw new BadRequestException("Suppression impossible : Des événements actifs existent pour cette structure. " +
                        "Pour supprimer la structure, vous devez d'abord :\n" +
                        "1. Accéder à la liste des événements de votre structure\n" +
                        "2. Annuler tous les événements actifs (statut PUBLISHED)\n" +
                        "Une fois tous les événements actifs traités, vous pourrez supprimer la structure.");
            }

            // Sauvegarde des informations pour la notification avant l'anonymisation
            String originalStructureName = structure.getName();

            // NOUVELLE ÉTAPE : Récupérer les administrateurs via TeamMember pour la notification future.
            List<User> administratorsToNotify = teamMemberRepository.findByStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR)
                    .stream()
                    .map(edu.cda.project.ticklybackend.models.team.TeamMember::getUser)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
            log.info("Trouvé {} administrateur(s) à notifier pour la structure ID: {}", administratorsToNotify.size(), structureId);

            // 1. Dissolution des relations
            log.info("Suppression des favoris pour la structure ID: {}", structureId);
            favoriteRepository.deleteByStructure_Id(structureId);

            // Dissolution de l'équipe - convertit tous les membres (y compris les admins) en SPECTATOR
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

            structureRepository.save(structure);
            log.info("Anonymisation de la structure ID: {} terminée.", structureId);

            // 4. Communication finale aux administrateurs
            // La dissociation manuelle est désormais gérée par teamService.dissolveTeam()
            log.info("Envoi des notifications de suppression aux anciens administrateurs...");
            administratorsToNotify.forEach(admin ->
                    mailingService.sendStructureDeletionConfirmation(admin.getEmail(), admin.getFirstName(), originalStructureName)
            );

            log.warn("Suppression de la structure ID: {} terminée avec succès.", structureId);
            LoggingUtils.logMethodExit(log, "deleteStructure");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression de la structure avec ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public FileUploadResponseDto updateStructureLogo(Long structureId, MultipartFile file) {
        LoggingUtils.logMethodEntry(log, "updateStructureLogo", "structureId", structureId, "file.name", file.getOriginalFilename());

        try {
            log.debug("Recherche de la structure pour mise à jour du logo. Structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            if (StringUtils.hasText(structure.getLogoPath())) {
                log.debug("Suppression de l'ancien logo pour la structure ID: {}", structureId);
                try {
                    fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
                    log.debug("Ancien logo supprimé avec succès pour la structure ID: {}", structureId);
                } catch (FileStorageException e) {
                    log.warn("Ancien logo non trouvé ou impossible à supprimer pour la structure ID {}: {}", structureId, e.getMessage());
                }
            }

            log.debug("Stockage du nouveau logo pour la structure ID: {}", structureId);
            String newLogoPath = fileStorageService.storeFile(file, LOGO_SUBDIR);
            structure.setLogoPath(newLogoPath);
            structureRepository.save(structure);
            log.info("Logo mis à jour pour la structure {} (ID: {}). Nouveau chemin: {}", structure.getName(), structureId, newLogoPath);

            FileUploadResponseDto result = new FileUploadResponseDto(
                    file.getOriginalFilename(),
                    fileStorageService.getFileUrl(newLogoPath, LOGO_SUBDIR),
                    "Logo de la structure mis à jour avec succès."
            );

            LoggingUtils.logMethodExit(log, "updateStructureLogo", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la mise à jour du logo pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public FileUploadResponseDto updateStructureCover(Long structureId, MultipartFile file) {
        LoggingUtils.logMethodEntry(log, "updateStructureCover", "structureId", structureId, "file.name", file.getOriginalFilename());

        try {
            log.debug("Recherche de la structure pour mise à jour de l'image de couverture. Structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            if (StringUtils.hasText(structure.getCoverPath())) {
                log.debug("Suppression de l'ancienne image de couverture pour la structure ID: {}", structureId);
                try {
                    fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
                    log.debug("Ancienne image de couverture supprimée avec succès pour la structure ID: {}", structureId);
                } catch (FileStorageException e) {
                    log.warn("Ancienne image de couverture non trouvée ou impossible à supprimer pour la structure ID {}: {}", structureId, e.getMessage());
                }
            }

            log.debug("Stockage de la nouvelle image de couverture pour la structure ID: {}", structureId);
            String newCoverPath = fileStorageService.storeFile(file, COVER_SUBDIR);
            structure.setCoverPath(newCoverPath);
            structureRepository.save(structure);
            log.info("Image de couverture mise à jour pour la structure {} (ID: {}). Nouveau chemin: {}", structure.getName(), structureId, newCoverPath);

            FileUploadResponseDto result = new FileUploadResponseDto(
                    file.getOriginalFilename(),
                    fileStorageService.getFileUrl(newCoverPath, COVER_SUBDIR),
                    "Image de couverture de la structure mise à jour avec succès."
            );

            LoggingUtils.logMethodExit(log, "updateStructureCover", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la mise à jour de l'image de couverture pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void removeStructureLogo(Long structureId) {
        LoggingUtils.logMethodEntry(log, "removeStructureLogo", "structureId", structureId);

        try {
            log.debug("Recherche de la structure pour suppression du logo. Structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            if (!StringUtils.hasText(structure.getLogoPath())) {
                log.warn("Tentative de suppression d'un logo inexistant pour la structure ID: {}", structureId);
                throw new ResourceNotFoundException("Logo", "structureId", structureId);
            }

            try {
                log.debug("Suppression du fichier logo pour la structure ID: {}", structureId);
                fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
                structure.setLogoPath(null);
                structureRepository.save(structure);
                log.info("Logo supprimé avec succès pour la structure {} (ID: {}).", structure.getName(), structureId);
                LoggingUtils.logMethodExit(log, "removeStructureLogo");
            } catch (FileStorageException e) {
                log.error("Impossible de supprimer le fichier logo pour la structure ID {}: {}", structureId, e.getMessage());
                LoggingUtils.logException(log, "Erreur lors de la suppression du fichier logo", e);
                throw new BadRequestException("Impossible de supprimer le fichier logo: " + e.getMessage());
            }
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression du logo pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void removeStructureCover(Long structureId) {
        LoggingUtils.logMethodEntry(log, "removeStructureCover", "structureId", structureId);

        try {
            log.debug("Recherche de la structure pour suppression de l'image de couverture. Structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            if (!StringUtils.hasText(structure.getCoverPath())) {
                log.warn("Tentative de suppression d'une image de couverture inexistante pour la structure ID: {}", structureId);
                throw new ResourceNotFoundException("Image de couverture", "structureId", structureId);
            }

            try {
                log.debug("Suppression du fichier image de couverture pour la structure ID: {}", structureId);
                fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
                structure.setCoverPath(null);
                structureRepository.save(structure);
                log.info("Image de couverture supprimée avec succès pour la structure {} (ID: {}).", structure.getName(), structureId);
                LoggingUtils.logMethodExit(log, "removeStructureCover");
            } catch (FileStorageException e) {
                log.error("Impossible de supprimer le fichier image de couverture pour la structure ID {}: {}", structureId, e.getMessage());
                LoggingUtils.logException(log, "Erreur lors de la suppression du fichier image de couverture", e);
                throw new BadRequestException("Impossible de supprimer le fichier image de couverture: " + e.getMessage());
            }
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression de l'image de couverture pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional
    public List<FileUploadResponseDto> addStructureGalleryImages(Long structureId, MultipartFile[] files) {
        LoggingUtils.logMethodEntry(log, "addStructureGalleryImages", "structureId", structureId, "files.length", files.length);

        try {
            log.debug("Recherche de la structure pour ajout d'images à la galerie. Structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            List<FileUploadResponseDto> results = new ArrayList<>();
            List<String> successfulUploads = new ArrayList<>();

            log.debug("Traitement de {} fichiers pour la galerie de la structure ID: {}", files.length, structureId);
            for (MultipartFile file : files) {
                try {
                    // Validation du fichier
                    if (file.isEmpty()) {
                        log.warn("Fichier vide ignoré: {}", file.getOriginalFilename());
                        results.add(new FileUploadResponseDto(
                                file.getOriginalFilename(),
                                null,
                                "Erreur: fichier vide"));
                        continue;
                    }

                    log.debug("Stockage du fichier: {} pour la galerie de la structure ID: {}", file.getOriginalFilename(), structureId);
                    // Stocker le fichier
                    String filename = fileStorageService.storeFile(file, GALLERY_SUBDIR);
                    String fileUrl = fileStorageService.getFileUrl(filename, GALLERY_SUBDIR);

                    // Ajouter à la liste des uploads réussis
                    successfulUploads.add(filename);
                    log.debug("Fichier stocké avec succès: {}, URL: {}", filename, fileUrl);

                    results.add(new FileUploadResponseDto(
                            file.getOriginalFilename(),
                            fileUrl,
                            "Image ajoutée avec succès"));

                } catch (Exception e) {
                    log.error("Erreur lors de l'upload du fichier {}: {}", file.getOriginalFilename(), e.getMessage());
                    LoggingUtils.logException(log, "Erreur lors de l'upload du fichier " + file.getOriginalFilename(), e);
                    results.add(new FileUploadResponseDto(
                            file.getOriginalFilename(),
                            null,
                            "Erreur: " + e.getMessage()));
                }
            }

            // Ajouter tous les fichiers uploadés avec succès à la galerie
            if (!successfulUploads.isEmpty()) {
                log.debug("Ajout de {} images à la galerie de la structure ID: {}", successfulUploads.size(), structureId);
                structure.getGalleryImagePaths().addAll(successfulUploads);
                structureRepository.save(structure);
                log.info("Ajout de {} images à la galerie de la structure {} réussi", successfulUploads.size(), structureId);
            } else {
                log.info("Aucune image n'a été ajoutée à la galerie de la structure ID: {}", structureId);
            }

            LoggingUtils.logMethodExit(log, "addStructureGalleryImages", results);
            return results;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'ajout d'images à la galerie pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void removeStructureGalleryImage(Long structureId, String imagePath) {
        LoggingUtils.logMethodEntry(log, "removeStructureGalleryImage", "structureId", structureId, "imagePath", imagePath);

        try {
            log.debug("Recherche de la structure pour suppression d'image de galerie. Structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            // imagePath est le nom de fichier unique (ex: uuid.jpg)
            if (!structure.getGalleryImagePaths().contains(imagePath)) {
                log.warn("Tentative de suppression d'une image de galerie inexistante. Structure ID: {}, Image path: {}", structureId, imagePath);
                throw new ResourceNotFoundException("Image", "path", imagePath + " in structure gallery " + structureId);
            }

            try {
                log.debug("Suppression du fichier image de galerie: {} pour la structure ID: {}", imagePath, structureId);
                fileStorageService.deleteFile(imagePath, GALLERY_SUBDIR);
                structure.getGalleryImagePaths().remove(imagePath);
                structureRepository.save(structure);
                log.info("Image {} supprimée de la galerie pour la structure {} (ID: {}).", imagePath, structure.getName(), structureId);
                LoggingUtils.logMethodExit(log, "removeStructureGalleryImage");
            } catch (FileStorageException e) {
                log.error("Impossible de supprimer le fichier image {} de la galerie pour la structure ID {}: {}", imagePath, structureId, e.getMessage());
                LoggingUtils.logException(log, "Erreur lors de la suppression du fichier image de galerie", e);
                throw new BadRequestException("Impossible de supprimer le fichier image de la galerie: " + e.getMessage());
            }
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression de l'image de galerie pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
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
        log.info("Espace {} créé pour la structure {} (ID: {}).", savedArea.getName(), structure.getName(), structureId);
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
        log.info("Modèle de zone {} créé pour l'espace {} (ID: {}).", savedTemplate.getName(), area.getName(), areaId);
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
