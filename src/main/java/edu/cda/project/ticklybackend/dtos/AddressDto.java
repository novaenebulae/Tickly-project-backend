package edu.cda.project.ticklybackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    private String country;
    private String city;
    private String postalCode;
    private String street;
    private String number;

}



