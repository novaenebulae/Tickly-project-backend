package edu.cda.project.ticklybackend.dtos.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String street;
    private String city;
    private String zipCode;
    private String country;
    // private Double latitude;
    // private Double longitude;
}