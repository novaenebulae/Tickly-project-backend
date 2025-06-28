package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
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
public class EventUpdateDto {

    @Schema(description = "Nouveau nom de l'événement.", example = "Concert de Rock Acoustique")
    @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères.")
    private String name;

    @Schema(description = "Nouvel ID de la catégorie de l'événement.", example = "2")
    private Long categoryId;

    @Schema(description = "Nouvelle description courte.", example = "Une soirée intime et acoustique.")
    @Size(max = 500, message = "La description courte ne doit pas dépasser 500 caractères.")
    private String shortDescription;

    @Schema(description = "Nouvelle description complète.")
    private String fullDescription;

    @Schema(description = "Nouvelle liste de mots-clés.", example = "[\"rock\", \"acoustique\"]")
    private List<String> tags;

    @Schema(description = "Nouvelle date de début (format ISO 8601 UTC).", example = "2025-07-16T19:00:00Z")
    @Future(message = "La date de début doit être dans le futur.")
    private ZonedDateTime startDate;

    @Schema(description = "Nouvelle date de fin (format ISO 8601 UTC).", example = "2025-07-16T22:00:00Z")
    private ZonedDateTime endDate;

    @Schema(description = "Nouvelle adresse spécifique à l'événement.")
    @Valid
    private AddressDto address;

    @NotNull
    @Schema(description = "Nouvelle configuration des zones d'audience (remplace l'ancienne).")
    @Size(min = 1, message = "An event must have at least one audience zone.")
    @Valid
    private List<EventAudienceZoneConfigDto> audienceZones;

    @Schema(description = "Mettre à jour l'affichage sur la page d'accueil.", example = "false")
    private Boolean displayOnHomepage;

    @Schema(description = "Mettre à jour la mise en avant de l'événement.", example = "true")
    private Boolean isFeaturedEvent;
}