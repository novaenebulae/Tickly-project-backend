package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateDto {

    @Schema(description = "New event name.", example = "Acoustic Rock Concert")
    @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères.")
    private String name;

    @Schema(description = "Updated list of category IDs.", example = "[1, 3, 5]")
    @Size(min = 1, message = "Au moins une catégorie doit être sélectionnée.")
    private List<Long> categoryIds;

    @Schema(description = "Updated short description.", example = "An intimate acoustic evening.")
    @Size(max = 500, message = "La description courte ne doit pas dépasser 500 caractères.")
    private String shortDescription;

    @Schema(description = "Updated full description.")
    private String fullDescription;

    @Schema(description = "Updated list of keywords.", example = "[\"rock\", \"acoustic\"]")
    private List<String> tags;

    @Schema(description = "New start date/time (ISO 8601 UTC).", example = "2025-07-16T19:00:00Z")
    @Future(message = "La date de début doit être dans le futur.")
    private ZonedDateTime startDate;

    @Schema(description = "New end date/time (ISO 8601 UTC).", example = "2025-07-16T22:00:00Z")
    private ZonedDateTime endDate;

    @Schema(description = "Updated event-specific address.")
    @Valid
    private AddressDto address;

    @Schema(description = "Updated audience zone configuration (replaces the previous one).")
    @Valid
    private List<EventAudienceZoneConfigDto> audienceZones;

    @Schema(description = "Update whether to display on the home page.", example = "false")
    private Boolean displayOnHomepage;

    @Schema(description = "Update whether the event is featured.", example = "true")
    private Boolean isFeaturedEvent;
}