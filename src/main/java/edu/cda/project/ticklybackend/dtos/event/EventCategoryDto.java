package edu.cda.project.ticklybackend.dtos.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Basic event category information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Basic event category information.")
public class EventCategoryDto {
    @Schema(description = "Category unique identifier.")
    private Long id;

    @Schema(description = "Category display name.")
    private String name;
}