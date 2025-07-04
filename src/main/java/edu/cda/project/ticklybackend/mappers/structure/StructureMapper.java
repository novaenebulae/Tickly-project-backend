package edu.cda.project.ticklybackend.mappers.structure;

import edu.cda.project.ticklybackend.dtos.structure.StructureCreationDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureUpdateDto;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {StructureTypeMapper.class, AreaMapper.class, AddressMapper.class})
public abstract class StructureMapper {

    // --- To DTO Mappings ---

    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "logoPath", target = "logoUrl", qualifiedByName = "logoUrlBuilder")
    @Mapping(source = "coverPath", target = "coverUrl", qualifiedByName = "coverUrlBuilder")
    @Mapping(target = "eventCount", ignore = true)
    public abstract StructureSummaryDto toSummaryDto(Structure entity, @Context FileStorageService fileStorageService);

    public abstract List<StructureSummaryDto> toSummaryDtoList(List<Structure> entities, @Context FileStorageService fileStorageService);

    @Mapping(source = "logoPath", target = "logoUrl", qualifiedByName = "logoUrlBuilder")
    @Mapping(source = "coverPath", target = "coverUrl", qualifiedByName = "coverUrlBuilder")
    @Mapping(source = "galleryImagePaths", target = "galleryImageUrls", qualifiedByName = "galleryUrlsBuilder")
    public abstract StructureDetailResponseDto toDetailDto(Structure entity, @Context FileStorageService fileStorageService);


    // --- To Entity Mappings (unchanged) ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "types", ignore = true)
    @Mapping(target = "areas", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    @Mapping(target = "coverPath", ignore = true)
    @Mapping(target = "galleryImagePaths", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    public abstract Structure toEntity(StructureCreationDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "types", ignore = true)
    @Mapping(target = "areas", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    @Mapping(target = "coverPath", ignore = true)
    @Mapping(target = "galleryImagePaths", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntityFromDto(StructureUpdateDto dto, @MappingTarget Structure entity);


    // --- Helper methods for File URLs ---

    @Named("logoUrlBuilder")
    protected String buildLogoUrl(String logoPath, @Context FileStorageService fileStorageService) {
        if (logoPath == null
                || logoPath.isBlank()
                || fileStorageService == null) {
            return null;
        }
        return fileStorageService.getFileUrl(logoPath, "structures/logos");
    }

    @Named("coverUrlBuilder")
    protected String buildCoverUrl(String coverPath, @Context FileStorageService fileStorageService) {
        if (coverPath == null
                || coverPath.isBlank()
                || fileStorageService == null) {
            return null;
        }
        return fileStorageService.getFileUrl(coverPath, "structures/covers");
    }

    @Named("galleryUrlsBuilder")
    protected List<String> buildGalleryImageUrls(List<String> galleryImagePaths, @Context FileStorageService fileStorageService) {
        if (galleryImagePaths == null
                || galleryImagePaths.isEmpty()
                || fileStorageService == null) {
            return null;
        }
        return galleryImagePaths.stream()
                .map(path -> fileStorageService.getFileUrl(path, "structures/gallery"))
                .collect(Collectors.toList());
    }
}