package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.enums.EventStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Request parameters to filter event search results.
 */
@Data
public class EventSearchParamsDto {
    @Parameter(description = "Free text search on event name, description, and tags.")
    private String query;

    @Parameter(description = "Category IDs used to filter events.")
    private List<Long> categoryIds;

    @Parameter(description = "Filter events starting after this date (ISO 8601 UTC).")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startDateAfter;

    @Parameter(description = "Filter events starting before this date (ISO 8601 UTC).")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startDateBefore;

    @Parameter(description = "Filter by event status.")
    private EventStatus status;

    @Parameter(description = "Filter events displayed on the home page.")
    private Boolean displayOnHomepage;

    @Parameter(description = "Filter featured events.")
    private Boolean isFeatured;

    @Parameter(description = "Filter by organizer structure ID.")
    private Long structureId;

    @Parameter(description = "Filter by city.")
    private String city;

    @Parameter(description = "Filter by tags (AND logic).")
    private List<String> tags;

}