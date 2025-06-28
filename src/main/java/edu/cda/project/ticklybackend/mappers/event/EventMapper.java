package edu.cda.project.ticklybackend.mappers.event;

import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.mappers.structure.StructureMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {StructureMapper.class, EventAudienceZoneMapper.class})
public abstract class EventMapper {

    @Autowired
    protected FileStorageService fileStorageService;


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "structure", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainPhotoPath", ignore = true)
    @Mapping(target = "eventPhotoPaths", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "audienceZones", ignore = true) // Géré manuellement dans le service
//    @Mapping(target = "address.latitude", ignore = true) // Ignore les champs non mappés
//    @Mapping(target = "address.longitude", ignore = true)
    public abstract Event toEntity(EventCreationDto dto);

    @Mapping(target = "mainPhotoUrl", source = "mainPhotoPath", qualifiedByName = "buildEventMainPhotoUrl")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "structureName", source = "structure.name")
    @Mapping(target = "structureId", source = "structure.id") // Ajout du mapping manquant
    public abstract EventSummaryDto toSummaryDto(Event event);

    @Mapping(target = "mainPhotoUrl", source = "mainPhotoPath", qualifiedByName = "buildEventMainPhotoUrl")
    @Mapping(target = "eventPhotoUrls", source = "eventPhotoPaths", qualifiedByName = "buildEventGalleryUrls")
    @Mapping(target = "audienceZones", source = "audienceZones")
    @Mapping(target = "areas", ignore = true)
    public abstract EventDetailResponseDto toDetailDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "structure", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainPhotoPath", ignore = true)
    @Mapping(target = "eventPhotoPaths", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "audienceZones", ignore = true) // Géré manuellement dans le service
//    @Mapping(target = "address.latitude", ignore = true)
//    @Mapping(target = "address.longitude", ignore = true)
    public abstract void updateEventFromDto(EventUpdateDto dto, @MappingTarget Event event);

    /**
     * Convertit un ZonedDateTime (utilisé dans les DTOs) en Instant (utilisé dans les entités).
     */
    protected Instant toInstant(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return zonedDateTime.toInstant();
    }

    /**
     * Convertit un Instant (depuis l'entité) en ZonedDateTime (pour les DTOs), en forçant le fuseau UTC (Z).
     */
    protected ZonedDateTime toZonedDateTime(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @AfterMapping
    protected void afterMapEventToDetailDto(Event event, @MappingTarget EventDetailResponseDto dto) {
        if (event.getAudienceZones() == null || event.getAudienceZones().isEmpty()) {
            return;
        }
        List<EventAreaSummaryDto> areaSummaries = event.getAudienceZones().stream()
                .map(eventZone -> eventZone.getTemplate().getArea())
                .distinct()
                .map(structureArea -> new EventAreaSummaryDto(structureArea.getId(), structureArea.getName()))
                .collect(Collectors.toList());
        dto.setAreas(areaSummaries);
    }

    @Named("buildEventMainPhotoUrl")
    protected String buildEventMainPhotoUrl(String path) {
        if (path == null || path.isBlank()) return null;
        return fileStorageService.getFileUrl(path, "events/main");
    }

    @Named("buildEventGalleryUrls")
    protected List<String> buildEventGalleryUrls(List<String> paths) {
        if (paths == null || paths.isEmpty()) return null;
        return paths.stream()
                .map(path -> fileStorageService.getFileUrl(path, "events/gallery"))
                .collect(Collectors.toList());
    }

}
