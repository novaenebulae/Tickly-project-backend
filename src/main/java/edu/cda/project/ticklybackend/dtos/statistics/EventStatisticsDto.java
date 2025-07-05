package edu.cda.project.ticklybackend.dtos.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for event-specific statistics.
 * Contains detailed charts and statistics for a single event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed statistics for a specific event")
public class EventStatisticsDto {

    @Schema(description = "ID of the event")
    private long eventId;

    @Schema(description = "Name of the event")
    private String eventName;

    @Schema(description = "Overall fill percentage of the event (0-100)")
    private double fillPercentage;

    @Schema(description = "Number of unique reservations for the event")
    private long uniqueReservationAmount;

    @Schema(description = "Number of attributed (valid) tickets for the event")
    private long attributedTicketsAmount;

    @Schema(description = "Number of scanned (used) tickets for the event")
    private long scannedTicketsNumber;

    @Schema(description = "Chart data for zone fill rates")
    private ChartJsDataDto zoneFillRateChart;

    @Schema(description = "Chart data for reservations over time")
    private ChartJsDataDto reservationsOverTimeChart;

    @Schema(description = "Chart data for ticket status distribution")
    private ChartJsDataDto ticketStatusChart;

    // Constructor that maintains backward compatibility
    public EventStatisticsDto(long eventId, String eventName, ChartJsDataDto zoneFillRateChart, 
                             ChartJsDataDto reservationsOverTimeChart, ChartJsDataDto ticketStatusChart) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.zoneFillRateChart = zoneFillRateChart;
        this.reservationsOverTimeChart = reservationsOverTimeChart;
        this.ticketStatusChart = ticketStatusChart;
    }
}
