package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.event.EventCategoryDto;
import edu.cda.project.ticklybackend.dtos.event.EventDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.event.EventSummaryDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.files.FileStorageService;
import edu.cda.project.ticklybackend.mappers.EventCategoryMapper;
import edu.cda.project.ticklybackend.mappers.EventMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventCategory;
import edu.cda.project.ticklybackend.repositories.EventCategoryRepository;
import edu.cda.project.ticklybackend.repositories.EventRepository;
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final FileStorageService fileStorageService; // Injecté pour le passer au mapper
    private final EventCategoryRepository eventCategoryRepository; // Ajouté
    private final EventCategoryMapper eventCategoryMapper; // Ajouté

    @Override
    public Page<EventSummaryDto> getAllPublishedEvents(Pageable pageable) {
        Page<Event> eventsPage = eventRepository.findByStatus(EventStatus.PUBLISHED, pageable);
        // Utilisation du mapper avec le contexte FileStorageService
        return eventsPage.map(event -> eventMapper.eventToEventSummaryDto(event, fileStorageService));
    }

    @Override
    public EventDetailResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            // Gérer la logique d'accès si l'événement n'est pas publié
            // Pour l'instant, on peut le cacher ou lever une exception spécifique si l'utilisateur n'est pas admin/créateur
            // Pour un endpoint public, on ne montre que les publiés.
            // Si un admin/créateur doit y accéder, il faudra une logique de permission.
            throw new ResourceNotFoundException("Event", "id", id + " (not published)");
        }
        return eventMapper.eventToEventDetailResponseDto(event, fileStorageService);
    }

    @Override
    public List<EventCategoryDto> getAllEventCategories() {
        List<EventCategory> categories = eventCategoryRepository.findAll();
        return eventCategoryMapper.eventCategoriesToEventCategoryDtos(categories);
    }
}