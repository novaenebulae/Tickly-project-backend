package edu.cda.project.ticklybackend.mappers.event;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.models.event.EventAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventAddressMapper {

    EventAddress toEntity(AddressDto dto);

    AddressDto toDto(EventAddress entity);

}