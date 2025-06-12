package edu.cda.project.ticklybackend.dtos.event;


import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDetailResponseDto {
    private Long id;
    private String name;
    private EventCategoryDto category;
    private String shortDescription;
    private String fullDescription;
    private List<String> tags;
    private Instant startDate;
    private Instant endDate;
    private AddressDto address; // Adresse de la structure ou de l'événement
    private StructureSummaryDto structure; // Informations sur la structure organisatrice
    private boolean isFreeEvent;
    private String mainPhotoUrl;
    private List<String> eventPhotoUrls;
    private EventStatus status;
    private boolean displayOnHomepage;
    private boolean isFeaturedEvent;
    private Instant createdAt;
    private Instant updatedAt;
    // private UserSummaryDto creator; // Si vous voulez afficher les infos du créateur
}