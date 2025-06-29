package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.List;

@Data
public class StructureSearchParamsDto {

    @Parameter(description = "Recherche textuelle sur le nom de la structure.")
    private String query;

    @Parameter(description = "Filtrer par type de structure (ex: 'Théâtre', 'Salle de concert').")
    private List<Long> typeIds;

}