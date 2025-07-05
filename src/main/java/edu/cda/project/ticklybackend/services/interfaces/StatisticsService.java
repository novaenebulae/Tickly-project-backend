package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.statistics.EventStatisticsDto;
import edu.cda.project.ticklybackend.dtos.statistics.StructureDashboardStatsDto;

/**
 * Service for generating statistics for structures and events.
 */
public interface StatisticsService {

    /**
     * Get dashboard statistics for a structure.
     *
     * @param structureId The ID of the structure
     * @return A DTO containing all KPIs and charts for the structure's dashboard
     */
    StructureDashboardStatsDto getStructureDashboardStats(Long structureId);

    /**
     * Get detailed statistics for a specific event.
     *
     * @param eventId The ID of the event
     * @return A DTO containing detailed charts and statistics for the event
     */
    EventStatisticsDto getEventStats(Long eventId);
}