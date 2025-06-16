package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.structure.StructureTypeDto;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@Tag(name = "Types de Structure", description = "API pour la gestion des types de structure.")
public class StructureTypeController {

    private final StructureService structureService;

    @Operation(summary = "Récupérer tous les types de structure disponibles",
            description = "Retourne une liste de tous les types de structure que les lieux événementiels peuvent adopter.")
    @ApiResponse(responseCode = "200", description = "Liste des types de structure récupérée avec succès.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = StructureTypeDto.class))))
    @GetMapping
    public ResponseEntity<List<StructureTypeDto>> getAllStructureTypes() {
        return ResponseEntity.ok(structureService.getAllStructureTypes());
    }
}