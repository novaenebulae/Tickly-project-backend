package edu.cda.project.ticklybackend.dtos.event;


import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
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
public class EventDetailResponseDto {
    private Long id;
    private String name;
    private List<EventCategoryDto> categories;
    private String shortDescription;
    private String fullDescription;
    private List<String> tags;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private AddressDto address; // Adresse de la structure ou de l'événement
    private StructureSummaryDto structure; // Informations sur la structure organisatrice
    private String mainPhotoUrl;
    private List<String> eventPhotoUrls;
    private EventStatus status;
    private boolean displayOnHomepage;
    private boolean isFeaturedEvent;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<EventAreaSummaryDto> areas;
    private List<EventAudienceZoneDto> audienceZones;
}