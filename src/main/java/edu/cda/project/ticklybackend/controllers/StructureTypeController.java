package edu.cda.project.ticklybackend.controllers; // Adaptez le package si vous avez un sous-package 'structure'

import edu.cda.project.ticklybackend.dtos.structure.StructureTypeDto;
import edu.cda.project.ticklybackend.services.interfaces.StructureTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/structure-types")
@RequiredArgsConstructor
@Tag(name = "Types de Structure", description = "Endpoints pour consulter les types de structures.")
public class StructureTypeController {

    private final StructureTypeService structureTypeService; // Ou StructureService

    @Operation(summary = "Lister tous les types de structures disponibles",
            description = "Récupère la liste de tous les types de structures (ex: Salle de concert, Théâtre).")
    @ApiResponse(responseCode = "200", description = "Liste des types de structure récupérée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    // Schema pour List<StructureTypeDto>
    @GetMapping
    public ResponseEntity<List<StructureTypeDto>> getAllStructureTypes() {
        List<StructureTypeDto> structureTypes = structureTypeService.getAllStructureTypes();
        return ResponseEntity.ok(structureTypes);
    }
}