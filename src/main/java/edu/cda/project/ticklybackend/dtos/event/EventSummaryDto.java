package edu.cda.project.ticklybackend.dtos.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventSummaryDto {
    private Long id;
    private String name;
    private List<EventCategoryDto> categories;
    private String shortDescription;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String city; // Ville de la structure (ou de l'événement si adresse spécifique)
    private Long structureId;
    private String structureName; // Nom de la structure
    private String mainPhotoUrl; // URL complète de la photo principale
    private EventStatus status;
    private boolean isFeaturedEvent;
}