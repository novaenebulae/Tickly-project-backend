package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse envoyée après la création réussie d'une structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de réponse après la création réussie d'une structure.")
public class StructureCreationResponseDto {

    @Schema(description = "ID de la structure nouvellement créée.", example = "42")
    private Long id;

    @Schema(description = "Nom de la structure nouvellement créée.", example = "Mon Nouveau Lieu")
    private String name;

    @Schema(description = "Message confirmant la création.", example = "Structure créée avec succès.")
    private String message;

    private Long structureId;

    private String accessToken;

    private long expiresIn;


}