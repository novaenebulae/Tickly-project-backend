package edu.cda.project.ticklybackend.dtos.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Helper DTO for the zone fill rate chart.
 * Represents data for a single audience zone in an event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data point for zone fill rate statistics")
public class ZoneFillRateDataPointDto {
    
    @Schema(description = "Name of the audience zone")
    private String zoneName;
    
    @Schema(description = "Number of tickets sold for this zone")
    private long ticketsSold;
    
    @Schema(description = "Total capacity of the zone")
    private int capacity;
    
    /**
     * Calculate the fill rate as a percentage.
     * 
     * @return The fill rate as a percentage (0-100)
     */
    @Schema(description = "Fill rate as a percentage (calculated)")
    public double getFillRate() {
        if (capacity == 0) return 0;
        return (double) ticketsSold / capacity * 100;
    }
}