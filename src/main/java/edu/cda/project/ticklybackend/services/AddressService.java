package edu.cda.project.ticklybackend.services;

import edu.cda.project.ticklybackend.dtos.AddressDto;
import edu.cda.project.ticklybackend.models.structure.Address;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    public Address convertToAddress(AddressDto dto) {

        if (dto == null) {
            return null;
        }
        Address address = new Address();
        address.setCountry(dto.getCountry());
        address.setCity(dto.getCity());
        address.setZipCode(dto.getPostalCode());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        return address;
    }
}
