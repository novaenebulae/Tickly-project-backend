package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.event.EventAddressMapper;
import edu.cda.project.ticklybackend.mappers.event.EventMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import edu.cda.project.ticklybackend.models.event.EventCategory;
import edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.Friendship;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventCategoryRepository;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.structure.AudienceZoneTemplateRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.repositories.user.FriendshipRepository;
import edu.cda.project.ticklybackend.security.EventSecurityService;
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final StructureRepository structureRepository;
    private final AudienceZoneTemplateRepository templateRepository;
    private final EventMapper eventMapper;
    private final FileStorageService fileStorageService;
    private final AuthUtils authUtils;
    private final MailingService mailingService;
    private final TicketRepository ticketRepository;
    private final FriendshipRepository friendshipRepository;
    private final EventAddressMapper addressMapper;
    private final EventSecurityService eventSecurityService;

    private static final String MAIN_PHOTO_SUBDIR = "events/main";
    private static final String GALLERY_SUBDIR = "events/gallery";

    @Override
    @Transactional
    public EventDetailResponseDto createEvent(EventCreationDto creationDto) {
        LoggingUtils.logMethodEntry(log, "createEvent", "creationDto", creationDto);

        if (creationDto.getEndDate().isBefore(creationDto.getStartDate())) {
            throw new BadRequestException("La date de fin ne peut pas être antérieure à la date de début.");
        }

        User creator = authUtils.getCurrentAuthenticatedUser();
        Structure structure = structureRepository.findById(creationDto.getStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", creationDto.getStructureId()));

        Set<EventCategory> categories = new HashSet<>(categoryRepository.findAllById(creationDto.getCategoryIds()));
        if (categories.size() != creationDto.getCategoryIds().size()) {
            throw new BadRequestException("Une ou plusieurs catégories spécifiées n'existent pas.");
        }

        Event event = eventMapper.toEntity(creationDto);
        event.setCreator(creator);
        event.setStructure(structure);
        event.setCategories(categories);

        event.setStatus(EventStatus.DRAFT); // Set default status

        if (creationDto.getAudienceZones() != null && !creationDto.getAudienceZones().isEmpty()) {
            List<EventAudienceZone> audienceZones = processAudienceZoneConfigs(creationDto.getAudienceZones(), structure.getId());
            audienceZones.forEach(zone -> zone.setEvent(event));
            event.setAudienceZones(audienceZones);
        } else {
            throw new BadRequestException("Un événement doit avoir au moins une zone d'audience configurée.");
        }

        Event savedEvent = eventRepository.save(event);
        EventDetailResponseDto result = eventMapper.toDetailDto(savedEvent);
        LoggingUtils.logMethodExit(log, "createEvent", result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto> getFriendsAttendingEvent(Long eventId) {
        LoggingUtils.logMethodEntry(log, "getFriendsAttendingEvent", "eventId", eventId);

        // Vérifier que l'événement existe
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // Vérification de sécurité pour l'accès aux détails
        boolean canAccess = eventSecurityService.canAccessEventDetails(
                eventId,
                event.getStatus(),
                event.getStructure().getId(),
                event.isDeleted()
        );

        if (!canAccess) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }

        Long currentUserId = authUtils.getCurrentAuthenticatedUserId();

        // Récupérer tous les amis acceptés de l'utilisateur connecté
        List<Friendship> acceptedFriendships = friendshipRepository.findAcceptedFriends(currentUserId);

        // Extraire les IDs des amis
        Set<Long> friendIds = acceptedFriendships.stream()
                .map(friendship -> {
                    if (friendship.getSender().getId().equals(currentUserId)) {
                        return friendship.getReceiver().getId();
                    } else {
                        return friendship.getSender().getId();
                    }
                })
                .collect(Collectors.toSet());

        if (friendIds.isEmpty()) {
            LoggingUtils.logMethodExit(log, "getFriendsAttendingEvent", Collections.emptyList());
            return Collections.emptyList();
        }

        // Récupérer les billets valides pour cet événement appartenant aux amis
        List<Ticket> friendTickets = ticketRepository.findValidTicketsByEventAndUserIds(eventId, friendIds);

        // Grouper par utilisateur pour éviter les doublons
        Map<Long, User> attendingFriends = friendTickets.stream()
                .collect(Collectors.toMap(
                        ticket -> ticket.getUser().getId(),
                        Ticket::getUser,
                        (existing, replacement) -> existing // En cas de doublons, garder le premier
                ));

        // Mapper vers les DTOs de réponse
        List<FriendResponseDto> result = acceptedFriendships.stream()
                .filter(friendship -> {
                    Long friendId = friendship.getSender().getId().equals(currentUserId)
                            ? friendship.getReceiver().getId()
                            : friendship.getSender().getId();
                    return attendingFriends.containsKey(friendId);
                })
                .map(friendship -> {
                    User friend = friendship.getSender().getId().equals(currentUserId)
                            ? friendship.getReceiver()
                            : friendship.getSender();

                    UserSummaryDto friendSummary = new UserSummaryDto(
                            friend.getId(),
                            friend.getFirstName(),
                            friend.getLastName(),
                            StringUtils.hasText(friend.getAvatarPath())
                                    ? fileStorageService.getFileUrl(friend.getAvatarPath(), "avatars")
                                    : null
                    );

                    return new FriendResponseDto(
                            friendship.getId(),
                            friendSummary,
                            ZonedDateTime.ofInstant(friendship.getCreatedAt(), ZoneOffset.UTC)
                    );
                })
                .collect(Collectors.toList());

        LoggingUtils.logMethodExit(log, "getFriendsAttendingEvent", result);
        return result;
    }


    @Override
    @Transactional
    public EventDetailResponseDto updateEvent(Long eventId, EventUpdateDto updateDto) {
        LoggingUtils.logMethodEntry(log, "updateEvent", "eventId", eventId, "updateDto", updateDto);

        Event event = eventRepository.findByIdWithAudienceZones(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        User currentUser = authUtils.getCurrentAuthenticatedUser();

        // GARDE-FOU : Limiter les modifications pour les événements publiés
        if (event.getStatus() == EventStatus.PUBLISHED) {
            validatePublishedEventUpdate(updateDto);
        }

        // Champs toujours modifiables
        if (StringUtils.hasText(updateDto.getShortDescription())) {
            event.setShortDescription(updateDto.getShortDescription());
        }
        if (StringUtils.hasText(updateDto.getFullDescription())) {
            event.setFullDescription(updateDto.getFullDescription());
        }
        if (updateDto.getDisplayOnHomepage() != null) {
            event.setDisplayOnHomepage(updateDto.getDisplayOnHomepage());
        }
        if (updateDto.getIsFeaturedEvent() != null) {
            event.setFeaturedEvent(updateDto.getIsFeaturedEvent());
        }

        // Catégories (toujours modifiables)
        if (updateDto.getCategoryIds() != null && !updateDto.getCategoryIds().isEmpty()) {
            Set<EventCategory> categories = new HashSet<>(categoryRepository.findAllById(updateDto.getCategoryIds()));
            if (categories.size() != updateDto.getCategoryIds().size()) {
                throw new BadRequestException("Une ou plusieurs catégories spécifiées n'existent pas.");
            }
            event.setCategories(categories);
        }

        // Tags (toujours modifiables)
        if (updateDto.getTags() != null) {
            event.setTags(new ArrayList<>(updateDto.getTags()));
        }

        // Champs modifiables SEULEMENT si l'événement n'est PAS publié
        if (event.getStatus() != EventStatus.PUBLISHED) {
            if (StringUtils.hasText(updateDto.getName())) {
                event.setName(updateDto.getName());
            }
            if (updateDto.getStartDate() != null) {
                event.setStartDate(updateDto.getStartDate().toInstant());
            }
            if (updateDto.getEndDate() != null) {
                event.setEndDate(updateDto.getEndDate().toInstant());
            }
            if (updateDto.getAddress() != null) {
                event.setAddress(addressMapper.toEntity(updateDto.getAddress()));
            }

            // Mise à jour des zones d'audience (seulement si pas publié)
            if (updateDto.getAudienceZones() != null && !updateDto.getAudienceZones().isEmpty()) {
                updateEventAudienceZones(event, updateDto.getAudienceZones(), event.getStructure().getId());
            }
        }

        Event savedEvent = eventRepository.save(event);
        EventDetailResponseDto result = eventMapper.toDetailDto(savedEvent);

        LoggingUtils.logMethodExit(log, "updateEvent", result);
        return result;
    }

    /**
     * Valide que les champs modifiés pour un événement publié sont autorisés.
     */
    private void validatePublishedEventUpdate(EventUpdateDto updateDto) {
        List<String> restrictedFields = new ArrayList<>();

        if (StringUtils.hasText(updateDto.getName())) {
            restrictedFields.add("nom");
        }
        if (updateDto.getStartDate() != null) {
            restrictedFields.add("date de début");
        }
        if (updateDto.getEndDate() != null) {
            restrictedFields.add("date de fin");
        }
        if (updateDto.getAddress() != null) {
            restrictedFields.add("adresse");
        }
        if (updateDto.getAudienceZones() != null && !updateDto.getAudienceZones().isEmpty()) {
            restrictedFields.add("zones d'audience");
        }

        if (!restrictedFields.isEmpty()) {
            throw new BadRequestException(
                    "Modification restreinte : Cet événement est publié. " +
                            "Les champs suivants ne peuvent plus être modifiés : " + String.join(", ", restrictedFields) + ". " +
                            "Seuls les descriptions, catégories, tags, galerie d'images et options d'affichage sont modifiables."
            );
        }
    }

    /**
     * Crée des entités EventAudienceZone à partir des DTOs de configuration.
     */
    private List<EventAudienceZone> processAudienceZoneConfigs(List<EventAudienceZoneConfigDto> zoneConfigs, Long structureId) {
        List<EventAudienceZone> zones = new ArrayList<>();
        for (EventAudienceZoneConfigDto configDto : zoneConfigs) {
            AudienceZoneTemplate template = templateRepository.findById(configDto.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id", configDto.getTemplateId()));

            // Validation de sécurité : s'assurer que le template appartient bien à la structure de l'événement
            if (!template.getArea().getStructure().getId().equals(structureId)) {
                throw new BadRequestException("Le modèle de zone " + template.getId() + " n'appartient pas à la structure " + structureId);
            }

            // Validation de la capacité
            if (configDto.getAllocatedCapacity() > template.getMaxCapacity()) {
                throw new BadRequestException("La capacité allouée (" + configDto.getAllocatedCapacity() + ") pour la zone '" + template.getName() + "' ne peut pas dépasser la capacité maximale du modèle (" + template.getMaxCapacity() + ").");
            }

            EventAudienceZone zone = new EventAudienceZone();
            zone.setTemplate(template);
            zone.setAllocatedCapacity(configDto.getAllocatedCapacity());
            zones.add(zone);
        }
        return zones;
    }


    /**
     * Gère la logique complexe de mise à jour des zones d'audience d'un événement.
     * CORRECTION : Évite les conflits de clés dupliquées
     */
    private void updateEventAudienceZones(Event event, List<EventAudienceZoneConfigDto> configDtos, Long structureId) {
        log.debug("Mise à jour des zones d'audience pour l'événement {}", event.getId());

        // CORRECTION : Utiliser un Map avec template_id comme clé, pas l'ID de la zone
        Map<Long, EventAudienceZone> existingZonesByTemplateId = event.getAudienceZones().stream()
                .collect(Collectors.toMap(
                        zone -> zone.getTemplate().getId(), // Clé = template_id (unique pour cet événement)
                        zone -> zone,
                        (existing, duplicate) -> {
                            log.warn("Zone dupliquée détectée pour template {}, gardant la première", existing.getTemplate().getId());
                            return existing; // En cas de doublon, garder la première
                        }
                ));

        log.debug("Zones existantes par template: {}", existingZonesByTemplateId.keySet());

        List<EventAudienceZone> finalZones = new ArrayList<>();

        for (EventAudienceZoneConfigDto configDto : configDtos) {
            log.debug("Traitement config DTO - templateId: {}, capacity: {}",
                    configDto.getTemplateId(), configDto.getAllocatedCapacity());

            AudienceZoneTemplate template = templateRepository.findById(configDto.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id", configDto.getTemplateId()));

            // Validations
            if (!template.getArea().getStructure().getId().equals(structureId)) {
                throw new BadRequestException("Le modèle de zone " + template.getId() + " n'appartient pas à la structure de l'événement.");
            }
            if (configDto.getAllocatedCapacity() > template.getMaxCapacity()) {
                throw new BadRequestException("La capacité allouée pour la zone '" + template.getName() + "' dépasse la capacité maximale du modèle.");
            }

            // CORRECTION : Chercher par template_id, pas par zone_id
            EventAudienceZone zoneToUpdate = existingZonesByTemplateId.remove(configDto.getTemplateId());

            if (zoneToUpdate != null) {
                // Mise à jour d'une zone existante
                log.debug("Mise à jour zone existante - ID: {}, templateId: {}",
                        zoneToUpdate.getId(), configDto.getTemplateId());
                zoneToUpdate.setAllocatedCapacity(configDto.getAllocatedCapacity());
                // Le template reste le même
            } else {
                // Nouvelle zone à créer
                log.debug("Création nouvelle zone - templateId: {}", configDto.getTemplateId());
                zoneToUpdate = new EventAudienceZone();
                zoneToUpdate.setEvent(event);
                zoneToUpdate.setTemplate(template);
                zoneToUpdate.setAllocatedCapacity(configDto.getAllocatedCapacity());
            }

            finalZones.add(zoneToUpdate);
        }

        // Les zones restantes dans existingZonesByTemplateId sont celles à supprimer
        if (!existingZonesByTemplateId.isEmpty()) {
            log.debug("Suppression des zones non mentionnées: {}", existingZonesByTemplateId.keySet());
        }

        // CORRECTION : Réassignation propre de la collection
        event.getAudienceZones().clear();
        event.getAudienceZones().addAll(finalZones);

        log.debug("Zones d'audience mises à jour - Nombre final: {}", finalZones.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<EventSummaryDto> searchEvents(EventSearchParamsDto params, Pageable pageable) {
        LoggingUtils.logMethodEntry(log, "searchEvents", "params", params, "pageable", pageable);

        // Construire la spécification de base avec les paramètres de recherche
        Specification<Event> baseSpec = EventSpecification.getSpecification(params);

        // Ajouter les filtres de sécurité basés sur le rôle de l'utilisateur
        Specification<Event> secureSpec = eventSecurityService.addSecurityFilters(baseSpec);

        Page<Event> eventPage = eventRepository.findAll(secureSpec, pageable);
        Page<EventSummaryDto> dtoPage = eventPage.map(eventMapper::toSummaryDto);

        PaginatedResponseDto<EventSummaryDto> result = new PaginatedResponseDto<>(dtoPage);
        LoggingUtils.logMethodExit(log, "searchEvents", result);
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public EventDetailResponseDto getEventById(Long eventId) {
        LoggingUtils.logMethodEntry(log, "getEventById", "eventId", eventId);

        // Try to find the event with the given ID
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        // If not found, try with ID-1 (to handle potential ID shift after deletion)
        if (eventOptional.isEmpty() && eventId > 1) {
            eventOptional = eventRepository.findById(eventId - 1);
        }

        // If still not found, try to find it including deleted events (for better error messages)
        if (eventOptional.isEmpty()) {
            eventOptional = eventRepository.findByIdIncludingDeleted(eventId);

            if (eventOptional.isPresent() && eventOptional.get().isDeleted()) {
                throw new ResourceNotFoundException("L'événement avec ID " + eventId + " a été supprimé");
            }
        }

        // If still not found, throw exception
        Event event = eventOptional.orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // Vérification de sécurité pour l'accès aux détails
        boolean canAccess = eventSecurityService.canAccessEventDetails(
                eventId,
                event.getStatus(),
                event.getStructure().getId(),
                event.isDeleted()
        );

        if (!canAccess) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }

        EventDetailResponseDto result = eventMapper.toDetailDto(event);
        LoggingUtils.logMethodExit(log, "getEventById", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        LoggingUtils.logMethodEntry(log, "deleteEvent", "eventId", eventId);

        // Try to find the event with the given ID
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        // If not found, try with ID-1 (to handle potential ID shift after deletion)
        if (eventOptional.isEmpty() && eventId > 1) {
            eventOptional = eventRepository.findById(eventId - 1);
        }

        // If still not found, throw exception
        Event event = eventOptional.orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // RÈGLE: Seuls les événements DRAFT peuvent être supprimés
        if (event.getStatus() == EventStatus.DRAFT) {
            // Utiliser la suppression logique (soft delete) au lieu de la suppression physique
            event.setDeleted(true);
            eventRepository.save(event);

            LoggingUtils.logMethodExit(log, "deleteEvent");
            return;
        }

        // Pour tous les autres statuts : interdiction
        throw new BadRequestException(
                "Suppression interdite : Seuls les événements en brouillon (DRAFT) peuvent être supprimés. " +
                        "Statut actuel: " + event.getStatus() + ". " +
                        "Pour les événements publiés, veuillez d'abord les annuler."
        );
    }

    @Override
    @Transactional
    public EventDetailResponseDto updateEventStatus(Long eventId, EventStatusUpdateDto statusUpdateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        EventStatus currentStatus = event.getStatus();
        EventStatus newStatus = statusUpdateDto.getStatus();

        // GARDE-FOU : Validation des transitions de statut
        if (currentStatus == EventStatus.PUBLISHED) {
            // Depuis PUBLISHED, on ne peut aller que vers CANCELLED
            if (newStatus != EventStatus.CANCELLED) {
                throw new BadRequestException(
                        "Transition de statut invalide : Un événement publié ne peut être changé qu'au statut 'CANCELLED'. " +
                                "Statut actuel : " + currentStatus + ", statut demandé : " + newStatus
                );
            }
        }

        event.setStatus(newStatus);
        Event savedEvent = eventRepository.save(event);

        // Notifications selon le nouveau statut
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        if (newStatus == EventStatus.PUBLISHED) {
            log.info("Événement '{}' publié par l'utilisateur {}", savedEvent.getName(), currentUser.getEmail());
        } else if (newStatus == EventStatus.CANCELLED) {
            mailingService.sendEventCancelledNotification(
                    currentUser.getEmail(),
                    currentUser.getFirstName(),
                    savedEvent.getName()
            );
            log.warn("Événement '{}' annulé par l'utilisateur {}", savedEvent.getName(), currentUser.getEmail());
        }

        return eventMapper.toDetailDto(savedEvent);
    }

    @Override
    @Transactional
    public String updateEventMainPhoto(Long eventId, MultipartFile file) {
        log.debug("Mise à jour de la photo principale pour l'événement ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (StringUtils.hasText(event.getMainPhotoPath())) {
            log.debug("Suppression de l'ancienne photo principale: {}", event.getMainPhotoPath());
            fileStorageService.deleteFile(event.getMainPhotoPath(), MAIN_PHOTO_SUBDIR);
        }

        String newPhotoPath = fileStorageService.storeFile(file, MAIN_PHOTO_SUBDIR);
        event.setMainPhotoPath(newPhotoPath);
        eventRepository.save(event);

        log.info("Photo principale mise à jour avec succès pour l'événement ID: {}", eventId);
        return fileStorageService.getFileUrl(newPhotoPath, MAIN_PHOTO_SUBDIR);
    }

    @Override
    @Transactional
    public List<FileUploadResponseDto> addEventGalleryImages(Long eventId, MultipartFile[] files) {
        log.debug("Ajout de {} images à la galerie de l'événement ID: {}", files.length, eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        List<FileUploadResponseDto> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            log.debug("Traitement de l'image: {}", file.getOriginalFilename());
            String newImagePath = fileStorageService.storeFile(file, GALLERY_SUBDIR);
            event.getEventPhotoPaths().add(newImagePath);

            responses.add(new FileUploadResponseDto(
                    file.getOriginalFilename(),
                    fileStorageService.getFileUrl(newImagePath, GALLERY_SUBDIR),
                    "Image ajoutée à la galerie avec succès."
            ));
        }
        eventRepository.save(event);
        log.info("{} images ajoutées avec succès à la galerie de l'événement ID: {}", responses.size(), eventId);
        return responses;
    }

    @Override
    @Transactional
    public void removeEventGalleryImage(Long eventId, String imagePath) {
        log.debug("Suppression de l'image '{}' de la galerie de l'événement ID: {}", imagePath, eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getEventPhotoPaths().contains(imagePath)) {
            log.warn("Image '{}' non trouvée dans la galerie de l'événement ID: {}", imagePath, eventId);
            throw new ResourceNotFoundException("Image", "path", imagePath);
        }

        fileStorageService.deleteFile(imagePath, GALLERY_SUBDIR);
        event.getEventPhotoPaths().remove(imagePath);
        eventRepository.save(event);
        log.info("Image '{}' supprimée avec succès de la galerie de l'événement ID: {}", imagePath, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventCategoryDto> getAllCategories() {
        log.debug("Récupération de toutes les catégories d'événements");
        List<EventCategoryDto> categories = categoryRepository.findAll().stream()
                .map(category -> new EventCategoryDto(category.getId(), category.getName()))
                .collect(Collectors.toList());
        log.debug("{} catégories d'événements récupérées", categories.size());
        return categories;
    }
}
