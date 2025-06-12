package edu.cda.project.ticklybackend.mappers;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
import edu.cda.project.ticklybackend.files.FileStorageService;
import edu.cda.project.ticklybackend.models.structure.Address;
import edu.cda.project.ticklybackend.models.structure.Structure;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {StructureTypeMapper.class})
public abstract class StructureMapper {

    @Autowired
    protected FileStorageService fileStorageService; // Pour les URLs des images de structure

    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "logoUrl", source = "logoPath", qualifiedByName = "buildStructureLogoUrl")
    public abstract StructureSummaryDto structureToStructureSummaryDto(Structure structure, @Context FileStorageService fsService);

    // Cette méthode est utilisée par EventMapper
    @Named("structureAddressToAddressDto")
    public AddressDto addressToAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        AddressDto dto = new AddressDto();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        return dto;
    }

    @Named("buildStructureLogoUrl")
    protected String buildStructureLogoUrl(String logoPath, @Context FileStorageService fsService) {
        if (logoPath == null
                || logoPath.isBlank()
                || fsService == null) {
            return null;
        }
        return fsService.getFileUrl(logoPath, "structures/logos");
    }
}