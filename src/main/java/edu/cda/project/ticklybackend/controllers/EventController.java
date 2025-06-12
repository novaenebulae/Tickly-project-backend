package edu.cda.project.ticklybackend.controllers; // Adaptez le package si vous avez un sous-package 'event'

import edu.cda.project.ticklybackend.dtos.event.EventCategoryDto;
import edu.cda.project.ticklybackend.dtos.event.EventDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.event.EventSummaryDto;
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Événements Publics", description = "Endpoints pour consulter les événements et leurs catégories.")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Lister tous les événements publiés",
            description = "Récupère une liste paginée de tous les événements actuellement publiés.")
    @ApiResponse(responseCode = "200", description = "Liste des événements récupérée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    // Schema pour Page<EventSummaryDto>
    @GetMapping
    public ResponseEntity<Page<EventSummaryDto>> getAllPublishedEvents(
            @ParameterObject Pageable pageable) {
        Page<EventSummaryDto> eventsPage = eventService.getAllPublishedEvents(pageable);
        return ResponseEntity.ok(eventsPage);
    }

    @Operation(summary = "Récupérer les détails d'un événement publié",
            description = "Récupère les informations détaillées d'un événement spécifique par son ID, s'il est publié.")
    @ApiResponse(responseCode = "200", description = "Détails de l'événement récupérés",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Événement non trouvé ou non publié")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponseDto> getEventById(
            @Parameter(description = "ID de l'événement à récupérer") @PathVariable Long eventId) {
        EventDetailResponseDto eventDetail = eventService.getEventById(eventId);
        return ResponseEntity.ok(eventDetail);
    }

    @Operation(summary = "Lister toutes les catégories d'événements",
            description = "Récupère la liste de toutes les catégories d'événements disponibles.")
    @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    // Schema pour List<EventCategoryDto>
    @GetMapping("/categories") // Endpoint distinct pour les catégories
    public ResponseEntity<List<EventCategoryDto>> getAllEventCategories() {
        List<EventCategoryDto> categories = eventService.getAllEventCategories();
        return ResponseEntity.ok(categories);
    }
}