package edu.cda.project.ticklybackend.dtos.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StructureSummaryDto {
    private Long id;
    private String name;
    private List<StructureTypeDto> types;
    private String city;
    private String logoUrl;
    // private String coverUrl;
    // private boolean isActive;
    // private Integer eventCount;
}