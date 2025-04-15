package edu.cda.project.ticklybackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StructureCreationDto {
    private String name;
    private String description;
    private AddressDto address;
    private List<Integer> typeIds;
}
