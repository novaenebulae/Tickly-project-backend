package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.event.EventCategoryDto;
import edu.cda.project.ticklybackend.dtos.event.EventDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.event.EventSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {
    Page<EventSummaryDto> getAllPublishedEvents(Pageable pageable);

    // Page<EventSummaryDto> searchEvents(EventSearchParamsDto params, Pageable pageable); // Pour recherche avancée
    EventDetailResponseDto getEventById(Long id);

    List<EventCategoryDto> getAllEventCategories(); // Ajouté pour l'endpoint des catégories
}