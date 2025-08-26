package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after a structure is successfully created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after a structure is successfully created.")
public class StructureCreationResponseDto {

    @Schema(description = "Newly created structure ID.", example = "42")
    private Long id;

    @Schema(description = "Newly created structure name.", example = "My New Venue")
    private String name;

    @Schema(description = "Confirmation message.", example = "Structure created successfully.")
    private String message;

    private Long structureId;

    private String accessToken;

    private long expiresIn;


}