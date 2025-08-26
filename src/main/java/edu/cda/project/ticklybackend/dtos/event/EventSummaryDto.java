package edu.cda.project.ticklybackend.dtos.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Compact event representation used in listings and search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Compact event representation used in listings and search results.")
public class EventSummaryDto {
    @Schema(description = "Event identifier.")
    private Long id;

    @Schema(description = "Event name.")
    private String name;

    @Schema(description = "Event categories.")
    private List<EventCategoryDto> categories;

    @Schema(description = "Short description.")
    private String shortDescription;

    @Schema(description = "Start date/time (ISO 8601 UTC).")
    private ZonedDateTime startDate;

    @Schema(description = "End date/time (ISO 8601 UTC).")
    private ZonedDateTime endDate;

    @Schema(description = "City where the event takes place.")
    private String city;

    @Schema(description = "Organizer structure ID.")
    private Long structureId;

    @Schema(description = "Organizer structure name.")
    private String structureName;

    @Schema(description = "Main photo URL.")
    private String mainPhotoUrl;

    @Schema(description = "Event status.")
    private EventStatus status;

    @Schema(description = "Whether the event is featured.")
    private boolean isFeaturedEvent;
}