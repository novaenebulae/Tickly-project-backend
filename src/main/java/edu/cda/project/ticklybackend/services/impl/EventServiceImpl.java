package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.event.EventAudienceZoneMapper;
import edu.cda.project.ticklybackend.mappers.event.EventMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import edu.cda.project.ticklybackend.models.event.EventCategory;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventCategoryRepository;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final StructureRepository structureRepository;
    private final EventMapper eventMapper;
    private final EventAudienceZoneMapper audienceZoneMapper;
    private final FileStorageService fileStorageService;
    private final AuthUtils authUtils;
    private final MailingService mailingService; // Ajouté pour les notifications
    private final TicketRepository ticketRepository; // Ajouté pour l'annulation

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
        EventCategory category = categoryRepository.findById(creationDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("EventCategory", "id", creationDto.getCategoryId()));

        Event event = eventMapper.toEntity(creationDto);
        event.setCreator(creator);
        event.setStructure(structure);
        event.setCategory(category);


        // Gérer les zones d'audience
        if (creationDto.getAudienceZones() != null) {
            List<EventAudienceZone> audienceZones = audienceZoneMapper.toEntityList(creationDto.getAudienceZones());
            audienceZones.forEach(zone -> zone.setEvent(event)); // Lier chaque zone à l'événement
            event.setAudienceZones(audienceZones);
        }

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDetailDto(savedEvent);
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
    public EventDetailResponseDto updateEvent(Long eventId, EventUpdateDto updateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        eventMapper.updateEventFromDto(updateDto, event);

        if (updateDto.getCategoryId() != null) {
            EventCategory category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventCategory", "id", updateDto.getCategoryId()));
            event.setCategory(category);
        }

        // Gérer la mise à jour des zones d'audience (remplacement complet pour simplifier)
        if (updateDto.getAudienceZones() != null) {
            event.getAudienceZones().clear();
            List<EventAudienceZone> newZones = audienceZoneMapper.toEntityList(updateDto.getAudienceZones());
            newZones.forEach(zone -> zone.setEvent(event));
            event.getAudienceZones().addAll(newZones);
        }


        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toDetailDto(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        log.warn("Tentative d'annulation de l'événement ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // 1. Logique de protection : ne pas annuler un événement déjà terminé ou annulé
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("L'événement est déjà " + event.getStatus().toString().toLowerCase() + " et ne peut pas être annulé.");
        }

        // 2. Changer le statut de l'événement
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Le statut de l'événement ID: {} a été changé à CANCELLED.", eventId);

        // 3. Traiter les billets existants
        List<Ticket> tickets = ticketRepository.findAllByEventId(eventId);
        if (!tickets.isEmpty()) {
            log.info("Annulation de {} billet(s) pour l'événement ID: {}.", tickets.size(), eventId);
            // Invalider tous les billets
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.CANCELLED);
            }
            ticketRepository.saveAll(tickets);

            // Regrouper par e-mail de l'acheteur pour n'envoyer qu'un seul e-mail par réservation
            Map<String, List<Ticket>> ticketsByBuyerEmail = tickets.stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getUser().getEmail()));

            // 4. Envoyer les notifications d'annulation
            ticketsByBuyerEmail.forEach((email, userTickets) -> {
                User user = userTickets.get(0).getUser();
                mailingService.sendEventCancelledNotification(email, user.getFirstName(), event.getName());
            });
            log.info("Notifications d'annulation envoyées à {} acheteur(s).", ticketsByBuyerEmail.size());
        }

        // Note : la suppression physique des images n'est pas effectuée lors d'une annulation
        // pour conserver l'historique. Elle pourrait l'être lors d'une suppression "hard" par un admin système.
    }

    @Override
    @Transactional
    public EventDetailResponseDto updateEventStatus(Long eventId, EventStatusUpdateDto statusUpdateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        event.setStatus(statusUpdateDto.getStatus());
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toDetailDto(updatedEvent);
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