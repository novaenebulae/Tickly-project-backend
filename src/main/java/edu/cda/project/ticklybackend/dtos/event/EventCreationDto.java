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

    @Schema(description = "Nom de l'événement.", example = "Concert de Rock Symphonique")
    @NotBlank(message = "Le nom de l'événement est requis.")
    @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères.")
    private String name;

    @Schema(description = "ID de la catégorie de l'événement.", example = "1")
    @NotNull(message = "L'ID de la catégorie est requis.")
    private Long categoryId;

    @Schema(description = "Description courte de l'événement pour les aperçus.", example = "Une fusion unique entre musique classique et rock.")
    @Size(max = 500, message = "La description courte ne doit pas dépasser 500 caractères.")
    private String shortDescription;

    @Schema(description = "Description complète et détaillée de l'événement.", example = "Vivez une soirée inoubliable avec l'orchestre philharmonique et le groupe 'The Unforgiven'...")
    @NotBlank(message = "La description complète est requise.")
    private String fullDescription;

    @Schema(description = "Liste de mots-clés pour la recherche.", example = "[\"rock\", \"symphonique\", \"concert\"]")
    private List<String> tags;

    @Schema(description = "Date et heure de début de l'événement (format ISO 8601 UTC).", example = "2025-07-15T18:00:00Z")
    @NotNull(message = "La date de début est requise.")
    @Future(message = "La date de début doit être dans le futur.")
    private ZonedDateTime startDate;

    @Schema(description = "Date et heure de fin de l'événement (format ISO 8601 UTC).", example = "2025-07-15T21:30:00Z")
    @NotNull(message = "La date de fin est requise.")
    private ZonedDateTime endDate;

    @Schema(description = "Adresse spécifique à l'événement (peut différer de celle de la structure).")
    @NotNull(message = "L'adresse de l'événement est requise.")
    @Valid
    private AddressDto address;

    @Schema(description = "ID de la structure organisatrice.", example = "3")
    @NotNull(message = "L'ID de la structure est requis.")
    private Long structureId;

    @Schema(description = "Configuration des zones d'audience pour l'événement.")
    @NotNull(message = "La configuration des zones d'audience est requise.")
    @Valid
    private List<EventAudienceZoneConfigDto> audienceZones;

    @Schema(description = "Indique si l'événement doit être affiché sur la page d'accueil.", example = "true")
    @NotNull(message = "Le champ 'displayOnHomepage' est requis.")
    private Boolean displayOnHomepage;

    @Schema(description = "Indique si l'événement doit être mis en avant.", example = "false")
    @NotNull(message = "Le champ 'isFeaturedEvent' est requis.")
    private Boolean isFeaturedEvent;
}