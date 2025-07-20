package edu.cda.project.ticklybackend.dtos.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for real-time ticket validation statistics.
 * Contains simplified statistics for the ticket validation panel.
 * This DTO is specifically designed for WebSocket communication in the ticket validation scenario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Real-time ticket validation statistics for an event")
public class EventTicketStatisticsDto {

    @Schema(description = "ID of the event")
    private long eventId;

    @Schema(description = "Name of the event")
    private String eventName;

    @Schema(description = "Total number of tickets for the event (valid + used)")
    private long totalTickets;

    @Schema(description = "Number of scanned (used) tickets for the event")
    private long scannedTickets;

    @Schema(description = "Number of remaining (valid) tickets for the event")
    private long remainingTickets;

    @Schema(description = "Percentage of used tickets compared to total tickets (0-100)")
    private double fillRate;
}