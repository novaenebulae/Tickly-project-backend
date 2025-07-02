package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.enums.TicketStatus;
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
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String MAIN_PHOTO_SUBDIR = "events/main";
    private static final String GALLERY_SUBDIR = "events/gallery";

    @Override
    @Transactional
    public EventDetailResponseDto createEvent(EventCreationDto creationDto) {
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
        log.info("Événement '{}' (ID: {}) créé par l'utilisateur '{}'.", savedEvent.getName(), savedEvent.getId(), creator.getEmail());
        return eventMapper.toDetailDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto> getFriendsAttendingEvent(Long eventId) {
        // Vérifier que l'événement existe
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

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
        return acceptedFriendships.stream()
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
                            friendship.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public EventDetailResponseDto updateEvent(Long eventId, EventUpdateDto updateDto) {
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
        log.info("Événement '{}' (ID: {}) mis à jour par l'utilisateur ID: {}", savedEvent.getName(), eventId, currentUser.getId());

        return eventMapper.toDetailDto(savedEvent);
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
     * Ajoute les nouvelles zones, met à jour les existantes et supprime celles qui ne sont plus dans la liste.
     */
    private void updateEventAudienceZones(Event event, List<EventAudienceZoneConfigDto> configDtos, Long structureId) {
        Map<Long, EventAudienceZone> existingZonesById = event.getAudienceZones().stream()
                .collect(Collectors.toMap(EventAudienceZone::getId, zone -> zone));

        List<EventAudienceZone> finalZones = new ArrayList<>();

        for (EventAudienceZoneConfigDto configDto : configDtos) {
            AudienceZoneTemplate template = templateRepository.findById(configDto.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("AudienceZoneTemplate", "id", configDto.getTemplateId()));

            // Validations
            if (!template.getArea().getStructure().getId().equals(structureId)) {
                throw new BadRequestException("Le modèle de zone " + template.getId() + " n'appartient pas à la structure de l'événement.");
            }
            if (configDto.getAllocatedCapacity() > template.getMaxCapacity()) {
                throw new BadRequestException("La capacité allouée pour la zone '" + template.getName() + "' dépasse la capacité maximale du modèle.");
            }

            EventAudienceZone zoneToUpdate;
            if (configDto.getId() != null) { // C'est une mise à jour d'une zone existante
                zoneToUpdate = existingZonesById.remove(configDto.getId());
                if (zoneToUpdate == null) {
                    throw new ResourceNotFoundException("EventAudienceZone", "id", configDto.getId());
                }
            } else { // C'est une nouvelle zone à ajouter
                zoneToUpdate = new EventAudienceZone();
                zoneToUpdate.setEvent(event);
            }

            zoneToUpdate.setTemplate(template);
            zoneToUpdate.setAllocatedCapacity(configDto.getAllocatedCapacity());
            finalZones.add(zoneToUpdate);
        }

        // À ce stade, `existingZonesById` ne contient que les zones qui n'étaient pas dans la liste de DTOs,
        // ce sont donc celles à supprimer.
        event.getAudienceZones().clear();
        event.getAudienceZones().addAll(finalZones);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<EventSummaryDto> searchEvents(EventSearchParamsDto params, Pageable pageable) {
        Specification<Event> spec = EventSpecification.getSpecification(params);
        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        Page<EventSummaryDto> dtoPage = eventPage.map(eventMapper::toSummaryDto);
        return new PaginatedResponseDto<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return eventMapper.toDetailDto(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        log.warn("Tentative d'annulation de l'événement ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("L'événement est déjà " + event.getStatus().toString().toLowerCase() + " et ne peut pas être annulé.");
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Le statut de l'événement ID: {} a été changé à CANCELLED.", eventId);

        List<Ticket> tickets = ticketRepository.findAllByEventId(eventId);
        if (!tickets.isEmpty()) {
            log.info("Annulation de {} billet(s) pour l'événement ID: {}.", tickets.size(), eventId);
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.CANCELLED);
            }
            ticketRepository.saveAll(tickets);

            Map<String, List<Ticket>> ticketsByBuyerEmail = tickets.stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getUser().getEmail()));

            ticketsByBuyerEmail.forEach((email, userTickets) -> {
                User user = userTickets.get(0).getUser();
                mailingService.sendEventCancelledNotification(email, user.getFirstName(), event.getName());
            });
            log.info("Notifications d'annulation envoyées à {} acheteur(s).", ticketsByBuyerEmail.size());
        }
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
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (StringUtils.hasText(event.getMainPhotoPath())) {
            fileStorageService.deleteFile(event.getMainPhotoPath(), MAIN_PHOTO_SUBDIR);
        }

        String newPhotoPath = fileStorageService.storeFile(file, MAIN_PHOTO_SUBDIR);
        event.setMainPhotoPath(newPhotoPath);
        eventRepository.save(event);

        return fileStorageService.getFileUrl(newPhotoPath, MAIN_PHOTO_SUBDIR);
    }

    @Transactional
    @Override
    public String addEventGalleryImage(Long eventId, MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        String newImagePath = fileStorageService.storeFile(file, GALLERY_SUBDIR);
        event.getEventPhotoPaths().add(newImagePath);
        eventRepository.save(event);

        return fileStorageService.getFileUrl(newImagePath, GALLERY_SUBDIR);
    }

    @Override
    @Transactional
    public void removeEventGalleryImage(Long eventId, String imagePath) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getEventPhotoPaths().contains(imagePath)) {
            throw new ResourceNotFoundException("Image", "path", imagePath);
        }

        fileStorageService.deleteFile(imagePath, GALLERY_SUBDIR);
        event.getEventPhotoPaths().remove(imagePath);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> new EventCategoryDto(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }
}
