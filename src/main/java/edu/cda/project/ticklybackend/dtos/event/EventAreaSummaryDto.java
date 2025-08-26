package edu.cda.project.ticklybackend.dtos.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a summary of a physical area used in an event.
 * Used in EventDetailResponseDto to list the relevant physical areas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAreaSummaryDto {

    /**
     * Unique ID of the physical area (StructureArea).
     */
    private Long id;

    /**
     * Name of the physical area (e.g., "Grande Scène Park").
     */
    private String name;
}
