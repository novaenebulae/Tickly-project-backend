package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.List;

/**
 * Request parameters to filter structure search results.
 */
@Data
public class StructureSearchParamsDto {

    @Parameter(description = "Free text search on structure name.")
    private String query;

    @Parameter(description = "Filter by structure type IDs.")
    private List<Long> typeIds;

}