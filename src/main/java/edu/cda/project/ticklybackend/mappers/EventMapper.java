package edu.cda.project.ticklybackend.mappers;

import edu.cda.project.ticklybackend.dtos.event.EventDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.event.EventSummaryDto;
import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.files.FileStorageService;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Address;
import edu.cda.project.ticklybackend.models.structure.Structure;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {EventCategoryMapper.class, StructureMapper.class})
public abstract class EventMapper {

    // Autowired pour que MapStruct puisse l'utiliser via @Context
    @Autowired
    protected FileStorageService fileStorageService;

    @Mapping(target = "city", source = "structure", qualifiedByName = "structureToCity")
    @Mapping(target = "structureName", source = "structure.name")
    @Mapping(target = "mainPhotoUrl", source = "mainPhotoPath", qualifiedByName = "buildEventPhotoUrl")
    public abstract EventSummaryDto eventToEventSummaryDto(Event event, @Context FileStorageService fsService);

    // Pour les listes
    public List<EventSummaryDto> eventsToEventSummaryDtos(List<Event> events, @Context FileStorageService fsService) {
        if (events == null) {
            return null;
        }
        // Utilisation de this pour appeler la méthode de mapping individuelle avec le contexte
        return events.stream()
                .map(event -> this.eventToEventSummaryDto(event, fsService))
                .collect(Collectors.toList());
    }


    @Mapping(target = "address", source = "structure.address", qualifiedByName = "structureAddressToAddressDto")
    // 'structure' sera mappé par StructureMapper (si défini dans uses) ou manuellement
    @Mapping(target = "mainPhotoUrl", source = "mainPhotoPath", qualifiedByName = "buildEventPhotoUrl")
    @Mapping(target = "eventPhotoUrls", source = "eventPhotoPaths", qualifiedByName = "buildEventGalleryUrls")
    public abstract EventDetailResponseDto eventToEventDetailResponseDto(Event event, @Context FileStorageService fsService);


    @Named("structureToCity")
    protected String structureToCity(Structure structure) {
        if (structure == null
                || structure.getAddress() == null) {
            return null;
        }
        return structure.getAddress().getCity();
    }

    @Named("structureAddressToAddressDto")
    protected AddressDto structureAddressToAddressDto(Address structureAddress) {
        if (structureAddress == null) {
            return null;
        }
        AddressDto dto = new AddressDto();
        dto.setStreet(structureAddress.getStreet());
        dto.setCity(structureAddress.getCity());
        dto.setZipCode(structureAddress.getZipCode());
        dto.setCountry(structureAddress.getCountry());
        return dto;
    }

    @Named("buildEventPhotoUrl")
    protected String buildEventPhotoUrl(String photoPath, @Context FileStorageService fsService) {
        if (photoPath == null ||
                photoPath.isBlank() ||
                fsService == null) {
            return null;
        }
        return fsService.getFileUrl(photoPath, "events/main"); // "events/main" est un exemple de sous-dossier
    }

    @Named("buildEventGalleryUrls")
    protected List<String> buildEventGalleryUrls(List<String> photoPaths, @Context FileStorageService fsService) {
        if (photoPaths == null
                || photoPaths.isEmpty()
                || fsService == null) {
            return null;
        }
        return photoPaths.stream()
                .map(path -> fsService.getFileUrl(path, "events/gallery")) // "events/gallery" pour la galerie
                .collect(Collectors.toList());
    }
}