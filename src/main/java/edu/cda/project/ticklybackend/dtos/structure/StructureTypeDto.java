package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour représenter un type de structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour un type de structure.")
public class StructureTypeDto {

    @Schema(description = "ID unique du type de structure.", example = "1")
    private Long id;

    @Schema(description = "Nom du type de structure.", example = "Salle de concert", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Nom ou chemin d'une icône associée (optionnel).", example = "concert-hall-icon")
    private String icon;
}