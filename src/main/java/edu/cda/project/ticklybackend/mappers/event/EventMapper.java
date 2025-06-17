package edu.cda.project.ticklybackend.mappers.event;

import edu.cda.project.ticklybackend.dtos.event.EventCreationDto;
import edu.cda.project.ticklybackend.dtos.event.EventDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.event.EventSummaryDto;
import edu.cda.project.ticklybackend.dtos.event.EventUpdateDto;
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

/**
 * Mapper pour la conversion entre les entités Event et leurs DTOs.
 * Utilise MapStruct pour générer l'implémentation.
 */
@Mapper(componentModel = "spring", uses = {StructureMapper.class, EventAudienceZoneMapper.class})
public abstract class EventMapper {

    @Autowired
    protected FileStorageService fileStorageService;


    /**
     * Convertit un DTO de création en entité Event.
     * Les champs complexes comme les relations et les chemins de fichiers sont ignorés
     * car ils doivent être gérés par la couche de service.
     *
     * @param dto Le DTO de création.
     * @return L'entité Event initialisée.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "structure", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainPhotoPath", ignore = true)
    @Mapping(target = "eventPhotoPaths", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "startDate", source = "startDate") // MapStruct utilisera toInstant()
    @Mapping(target = "endDate", source = "endDate")
    public abstract Event toEntity(EventCreationDto dto);

    /**
     * Convertit une entité Event en DTO résumé, optimisé pour les listes.
     *
     * @param event L'entité à convertir.
     * @return Le DTO résumé.
     */
    @Mapping(target = "mainPhotoUrl", source = "mainPhotoPath", qualifiedByName = "buildEventMainPhotoUrl")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "structureName", source = "structure.name")
    @Mapping(target = "startDate", source = "startDate") // MapStruct utilisera toZonedDateTime()
    @Mapping(target = "endDate", source = "endDate")
    public abstract EventSummaryDto toSummaryDto(Event event);

    /**
     * Convertit une entité Event en DTO détaillé.
     *
     * @param event L'entité à convertir.
     * @return Le DTO détaillé.
     */
    @Mapping(target = "mainPhotoUrl", source = "mainPhotoPath", qualifiedByName = "buildEventMainPhotoUrl")
    @Mapping(target = "eventPhotoUrls", source = "eventPhotoPaths", qualifiedByName = "buildEventGalleryUrls")
    @Mapping(target = "startDate", source = "startDate")   // MapStruct utilisera toZonedDateTime()
    @Mapping(target = "endDate", source = "endDate")       // MapStruct utilisera toZonedDateTime()
    @Mapping(target = "createdAt", source = "createdAt")   // MapStruct utilisera toZonedDateTime()
    @Mapping(target = "updatedAt", source = "updatedAt")
    public abstract EventDetailResponseDto toDetailDto(Event event);

    /**
     * Met à jour une entité Event à partir d'un DTO de mise à jour.
     * Ignore les champs nuls du DTO pour permettre des mises à jour partielles.
     *
     * @param dto   Le DTO contenant les mises à jour.
     * @param event L'entité à mettre à jour.
     */
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
    @Mapping(target = "startDate", source = "startDate") // MapStruct utilisera toInstant()
    @Mapping(target = "endDate", source = "endDate")
    public abstract void updateEventFromDto(EventUpdateDto dto, @MappingTarget Event event);

    /**
     * Convertit un ZonedDateTime en Instant.
     * Appelée automatiquement par MapStruct lors du mapping.
     */
    protected Instant toInstant(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return zonedDateTime.toInstant();
    }


    /**
     * Convertit un Instant en ZonedDateTime (au fuseau UTC).
     * Appelée automatiquement par MapStruct lors du mapping.
     */
    protected ZonedDateTime toZonedDateTime(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }


    /**
     * Construit l'URL complète pour la photo principale d'un événement.
     *
     * @param path Le chemin relatif du fichier.
     * @return L'URL complète, ou null si le chemin est vide.
     */
    @Named("buildEventMainPhotoUrl")
    protected String buildEventMainPhotoUrl(String path) {
        if (path == null || path.isBlank()) return null;
        return fileStorageService.getFileUrl(path, "events/main");
    }

    /**
     * Construit les URLs complètes pour les images de la galerie d'un événement.
     *
     * @param paths La liste des chemins relatifs des fichiers.
     * @return Une liste d'URLs complètes, ou null si la liste est vide.
     */
    @Named("buildEventGalleryUrls")
    protected List<String> buildEventGalleryUrls(List<String> paths) {
        if (paths == null || paths.isEmpty()) return null;
        return paths.stream()
                .map(path -> fileStorageService.getFileUrl(path, "events/gallery"))
                .collect(Collectors.toList());
    }
}