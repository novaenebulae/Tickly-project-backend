package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.statistics.EventStatisticsDto;
import edu.cda.project.ticklybackend.dtos.statistics.StructureDashboardStatsDto;
import edu.cda.project.ticklybackend.exceptions.AccessDeniedException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for statistics-related endpoints.
 * Provides access to structure and event statistics for administrators.
 */
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Endpoints for accessing statistics data")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Get dashboard statistics for a structure.
     *
     * @param structureId The ID of the structure
     * @return A DTO containing all KPIs and charts for the structure's dashboard
     */
    @GetMapping("/structure/{structureId}/dashboard")
    @PreAuthorize("@structureSecurityService.isStructureStaff(#structureId, authentication)")
    @Operation(
            summary = "Get structure dashboard statistics",
            description = "Returns a consolidated object containing all KPIs and global charts for a structure's main dashboard."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StructureDashboardStatsDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to access this structure's statistics"),
            @ApiResponse(responseCode = "404", description = "Structure not found")
    })
    public ResponseEntity<StructureDashboardStatsDto> getStructureDashboardStats(
            @Parameter(description = "ID of the structure", required = true)
            @PathVariable Long structureId) {

        log.info("Récupération des statistiques du tableau de bord pour la structure ID: {}", structureId);
        try {
            StructureDashboardStatsDto stats = statisticsService.getStructureDashboardStats(structureId);
            log.info("Statistiques du tableau de bord récupérées avec succès pour la structure ID: {}", structureId);
            return ResponseEntity.ok(stats);
        } catch (AccessDeniedException e) {
            log.warn("Accès refusé lors de la récupération des statistiques pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("Structure non trouvée lors de la récupération des statistiques - ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des statistiques pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get detailed statistics for a specific event.
     *
     * @param eventId The ID of the event
     * @return A DTO containing detailed charts and statistics for the event
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("@eventSecurityService.canAccessEventForStatistics(#eventId, authentication)")
    @Operation(
            summary = "Get event-specific statistics",
            description = "Returns detailed charts and statistics for a single event."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventStatisticsDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to access this event's statistics"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventStatisticsDto> getEventStats(
            @Parameter(description = "ID of the event", required = true)
            @PathVariable Long eventId) {

        log.info("Récupération des statistiques pour l'événement ID: {}", eventId);
        try {
            EventStatisticsDto stats = statisticsService.getEventStats(eventId);
            log.info("Statistiques récupérées avec succès pour l'événement ID: {}", eventId);
            return ResponseEntity.ok(stats);
        } catch (AccessDeniedException e) {
            log.warn("Accès refusé lors de la récupération des statistiques pour l'événement ID: {}: {}", eventId, e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("Événement non trouvé lors de la récupération des statistiques - ID: {}: {}", eventId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des statistiques pour l'événement ID: {}: {}", eventId, e.getMessage());
            throw e;
        }
    }
}
