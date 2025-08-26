package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreationDto {

    @Schema(description = "Event name.", example = "Symphonic Rock Concert")
    @NotBlank(message = "Le nom de l'événement est requis.")
    @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères.")
    private String name;

    @Schema(description = "List of category IDs for the event.", example = "[1, 3, 5]")
    @NotNull(message = "Au moins une catégorie est requise.")
    @Size(min = 1, message = "Au moins une catégorie doit être sélectionnée.")
    private List<Long> categoryIds;


    @Schema(description = "Short description for previews.", example = "A unique fusion of classical music and rock.")
    @Size(max = 500, message = "La description courte ne doit pas dépasser 500 caractères.")
    private String shortDescription;

    @Schema(description = "Full detailed description.", example = "Experience an unforgettable evening with the philharmonic orchestra and the band 'The Unforgiven'...")
    @NotBlank(message = "La description complète est requise.")
    private String fullDescription;

    @Schema(description = "Search keywords.", example = "[\"rock\", \"symphonic\", \"concert\"]")
    private List<String> tags;

    @Schema(description = "Event start date/time (ISO 8601 UTC).", example = "2025-07-15T18:00:00Z")
    @NotNull(message = "La date de début est requise.")
    @Future(message = "La date de début doit être dans le futur.")
    private ZonedDateTime startDate;

    @Schema(description = "Event end date/time (ISO 8601 UTC).", example = "2025-07-15T21:30:00Z")
    @NotNull(message = "La date de fin est requise.")
    private ZonedDateTime endDate;

    @Schema(description = "Event-specific address (may differ from structure address).")
    @NotNull(message = "L'adresse de l'événement est requise.")
    @Valid
    private AddressDto address;

    @Schema(description = "Organizer structure ID.", example = "3")
    @NotNull(message = "L'ID de la structure est requis.")
    private Long structureId;

    @Schema(description = "Audience zone configuration for the event.")
    @NotNull(message = "La configuration des zones d'audience est requise.")
    @Valid
    private List<EventAudienceZoneConfigDto> audienceZones;

    @Schema(description = "Whether to display the event on the home page.", example = "true")
    @NotNull(message = "Le champ 'displayOnHomepage' est requis.")
    private Boolean displayOnHomepage;

    @Schema(description = "Whether the event is featured.", example = "false")
    @NotNull(message = "Le champ 'isFeaturedEvent' est requis.")
    private Boolean isFeaturedEvent;
}