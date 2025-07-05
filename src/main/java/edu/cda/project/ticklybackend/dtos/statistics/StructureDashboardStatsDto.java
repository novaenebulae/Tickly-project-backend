package edu.cda.project.ticklybackend.dtos.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the main dashboard KPIs and charts for a structure.
 * Contains aggregated statistics about events, tickets, and attendance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistics for a structure's main dashboard")
public class StructureDashboardStatsDto {
    
    @Schema(description = "Number of upcoming events for the structure")
    private long upcomingEventsCount;
    
    @Schema(description = "Total number of tickets reserved across all events")
    private long totalTicketsReserved;
    
    @Schema(description = "Total number of expected attendees for upcoming events")
    private long totalExpectedAttendees;
    
    @Schema(description = "Average attendance rate as a percentage")
    private double averageAttendanceRate;
    
    @Schema(description = "Chart data for top events by ticket sales")
    private ChartJsDataDto topEventsChart;
    
    @Schema(description = "Chart data for attendance by event category")
    private ChartJsDataDto attendanceByCategoryChart;
}