package edu.cda.project.ticklybackend.dtos.event;

import edu.cda.project.ticklybackend.enums.EventStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO représentant les paramètres de recherche pour les événements.
 * Utilisé pour capturer les query params de l'endpoint de recherche.
 */
@Data
public class EventSearchParamsDto {
    @Parameter(description = "Recherche textuelle sur le nom, la description et les tags de l'événement.")
    private String query;

    @Parameter(description = "Liste d'IDs de catégories pour filtrer les événements.")
    private List<Long> categoryIds;

    @Parameter(description = "Filtrer les événements commençant après cette date (format ISO 8601 UTC).")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startDateAfter;

    @Parameter(description = "Filtrer les événements commençant avant cette date (format ISO 8601 UTC).")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startDateBefore;

    @Parameter(description = "Filtrer par statut de l'événement.")
    private EventStatus status;

    @Parameter(description = "Filtrer les événements affichés sur la page d'accueil.")
    private Boolean displayOnHomepage;

    @Parameter(description = "Filtrer les événements mis en avant.")
    private Boolean isFeatured;

    @Parameter(description = "Filtrer par ID de la structure organisatrice.")
    private Long structureId;

    @Parameter(description = "Filtrer par ville de l'événement.")
    private String city;

    @Parameter(description = "Filtrer par tags (logique ET).")
    private List<String> tags;

}