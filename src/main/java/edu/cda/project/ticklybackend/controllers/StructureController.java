package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.common.ErrorResponseDto;
import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/structures")
@RequiredArgsConstructor
@Tag(name = "Structures", description = "API pour la gestion des structures événementielles et de leurs composants.")
@SecurityRequirement(name = "bearerAuth")
public class StructureController {

    private final StructureService structureService;

    // ====== STRUCTURE CRUD ======

    @Operation(summary = "Créer une nouvelle structure",
            description = "Permet à un administrateur de structure (avec 'needsStructureSetup' à true) de créer sa structure. " +
                    "Le token JWT de l'utilisateur sera mis à jour implicitement côté serveur pour refléter l'association.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Structure créée avec succès", content = @Content(schema = @Schema(implementation = StructureCreationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Données de création invalides", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur n'est pas autorisé à créer une structure ou en a déjà une")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STRUCTURE_ADMINISTRATOR') and #authentication.principal.needsStructureSetup == true")
    public ResponseEntity<StructureCreationResponseDto> createStructure(
            @Parameter(description = "DTO pour la création de la structure", required = true)
            @Valid @RequestBody StructureCreationDto creationDto,
            Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        StructureCreationResponseDto responseDto = structureService.createStructure(creationDto, authenticatedUser.getEmail());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Lister toutes les structures",
            description = "Récupère une liste paginée et potentiellement filtrée de toutes les structures. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Liste des structures récupérée avec succès", content = @Content(schema = @Schema(implementation = Page.class)))
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<StructureSummaryDto>> getAllStructures(
            @ParameterObject Pageable pageable,
            @Parameter(description = "Filtres optionnels (non implémentés pour le moment). Ex: city=Paris&typeId=1")
            @RequestParam(required = false) Map<String, String> filters) {
        return ResponseEntity.ok(structureService.getAllStructures(pageable, filters));
    }

    @Operation(summary = "Récupérer les détails d'une structure",
            description = "Récupère les informations détaillées d'une structure par son ID. Endpoint public.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détails de la structure", content = @Content(schema = @Schema(implementation = StructureDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Structure non trouvée", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{structureId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<StructureDetailResponseDto> getStructureById(@PathVariable Long structureId) {
        return ResponseEntity.ok(structureService.getStructureById(structureId));
    }

    @Operation(summary = "Mettre à jour une structure (partiel)",
            description = "Met à jour partiellement une structure existante. Seuls les champs fournis seront modifiés. " +
                    "Accessible aux administrateurs système ou au propriétaire de la structure.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Structure mise à jour avec succès", content = @Content(schema = @Schema(implementation = StructureDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Structure non trouvée")
    })
    @PatchMapping("/{structureId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMINISTRATOR') or @structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<StructureDetailResponseDto> updateStructure(
            @PathVariable Long structureId,
            @Valid @RequestBody StructureUpdateDto updateDto,
            Authentication authentication) {
        return ResponseEntity.ok(structureService.updateStructure(structureId, updateDto));
    }

    @Operation(summary = "Supprimer une structure",
            description = "Supprime une structure et tous ses composants et fichiers associés. " +
                    "Accessible aux administrateurs système ou au propriétaire de la structure.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Structure supprimée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Structure non trouvée")
    })
    @DeleteMapping("/{structureId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMINISTRATOR') or @structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long structureId, Authentication authentication) {
        structureService.deleteStructure(structureId);
        return ResponseEntity.noContent().build();
    }

    // ====== FILE MANAGEMENT ======

    @Operation(summary = "Mettre à jour le logo d'une structure",
            description = "Remplace le logo actuel de la structure. Nécessite d'être le propriétaire.")
    @PostMapping(value = "/{structureId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<FileUploadResponseDto> updateStructureLogo(@PathVariable Long structureId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateStructureLogo(structureId, file));
    }

    @Operation(summary = "Mettre à jour l'image de couverture d'une structure",
            description = "Remplace l'image de couverture actuelle de la structure. Nécessite d'être le propriétaire.")
    @PostMapping(value = "/{structureId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<FileUploadResponseDto> updateStructureCover(@PathVariable Long structureId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateStructureCover(structureId, file));
    }

    @Operation(summary = "Ajouter une image à la galerie d'une structure",
            description = "Ajoute une nouvelle image à la galerie de la structure. Nécessite d'être le propriétaire.")
    @PostMapping(value = "/{structureId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<FileUploadResponseDto> addStructureGalleryImage(@PathVariable Long structureId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(structureService.addStructureGalleryImage(structureId, file));
    }

    @Operation(summary = "Supprimer une image de la galerie d'une structure",
            description = "Supprime une image spécifique de la galerie. Nécessite d'être le propriétaire.")
    @DeleteMapping("/{structureId}/gallery")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<Void> removeStructureGalleryImage(
            @PathVariable Long structureId,
            @Parameter(description = "Chemin de l'image à supprimer, tel que retourné dans les URLs de la galerie.", required = true)
            @RequestParam String imagePath,
            Authentication authentication) {
        structureService.removeStructureGalleryImage(structureId, imagePath);
        return ResponseEntity.noContent().build();
    }

    // ====== NESTED: STRUCTURE AREA ======

    @Operation(summary = "Créer un espace (Area) pour une structure",
            description = "Ajoute un nouvel espace physique (salle, scène...) à une structure. Nécessite d'être le propriétaire.")
    @PostMapping("/{structureId}/areas")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<AreaResponseDto> createArea(@PathVariable Long structureId, @Valid @RequestBody AreaCreationDto creationDto, Authentication authentication) {
        return new ResponseEntity<>(structureService.createArea(structureId, creationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Lister les espaces (Areas) d'une structure",
            description = "Récupère tous les espaces physiques configurés pour une structure donnée. Accessible au propriétaire ou aux services d'organisation.")
    @GetMapping("/{structureId}/areas")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id) or hasAuthority('ROLE_ORGANIZATION_SERVICE')")
    public ResponseEntity<List<AreaResponseDto>> getAreasByStructureId(@PathVariable Long structureId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAreasByStructureId(structureId));
    }

    @Operation(summary = "Récupérer un espace (Area) spécifique",
            description = "Récupère les détails d'un espace par son ID, dans le contexte d'une structure.")
    @GetMapping("/{structureId}/areas/{areaId}")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id) or hasAuthority('ROLE_ORGANIZATION_SERVICE')")
    public ResponseEntity<AreaResponseDto> getAreaById(@PathVariable Long structureId, @PathVariable Long areaId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAreaById(structureId, areaId));
    }

    @Operation(summary = "Mettre à jour un espace (Area)",
            description = "Met à jour partiellement un espace existant. Nécessite d'être le propriétaire de la structure parente.")
    @PatchMapping("/{structureId}/areas/{areaId}")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<AreaResponseDto> updateArea(@PathVariable Long structureId, @PathVariable Long areaId, @Valid @RequestBody AreaUpdateDto updateDto, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateArea(structureId, areaId, updateDto));
    }

    @Operation(summary = "Supprimer un espace (Area)",
            description = "Supprime un espace d'une structure. Nécessite d'être le propriétaire.")
    @DeleteMapping("/{structureId}/areas/{areaId}")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<Void> deleteArea(@PathVariable Long structureId, @PathVariable Long areaId, Authentication authentication) {
        structureService.deleteArea(structureId, areaId);
        return ResponseEntity.noContent().build();
    }

    // ====== NESTED: AUDIENCE ZONE TEMPLATE ======

    @Operation(summary = "Créer un modèle de zone d'audience",
            description = "Ajoute un nouveau modèle de zone (fosse, balcon...) à un espace spécifique. Nécessite d'être propriétaire de la structure.")
    @PostMapping("/{structureId}/areas/{areaId}/audience-zone-templates")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<AudienceZoneTemplateResponseDto> createAudienceZoneTemplate(
            @PathVariable Long structureId, @PathVariable Long areaId, @Valid @RequestBody AudienceZoneTemplateCreationDto creationDto, Authentication authentication) {
        return new ResponseEntity<>(structureService.createAudienceZoneTemplate(structureId, areaId, creationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Lister les modèles de zones d'un espace",
            description = "Récupère tous les modèles de zones d'audience pour un espace donné.")
    @GetMapping("/{structureId}/areas/{areaId}/audience-zone-templates")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id) or hasAuthority('ROLE_ORGANIZATION_SERVICE')")
    public ResponseEntity<List<AudienceZoneTemplateResponseDto>> getAudienceZoneTemplatesByAreaId(@PathVariable Long structureId, @PathVariable Long areaId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAudienceZoneTemplatesByAreaId(structureId, areaId));
    }

    @Operation(summary = "Récupérer un modèle de zone spécifique",
            description = "Récupère les détails d'un modèle de zone par son ID.")
    @GetMapping("/{structureId}/areas/{areaId}/audience-zone-templates/{templateId}")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id) or hasAuthority('ROLE_ORGANIZATION_SERVICE')")
    public ResponseEntity<AudienceZoneTemplateResponseDto> getAudienceZoneTemplateById(@PathVariable Long structureId, @PathVariable Long areaId, @PathVariable Long templateId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAudienceZoneTemplateById(structureId, areaId, templateId));
    }

    @Operation(summary = "Mettre à jour un modèle de zone",
            description = "Met à jour un modèle de zone existant. Nécessite d'être propriétaire.")
    @PatchMapping("/{structureId}/areas/{areaId}/audience-zone-templates/{templateId}")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<AudienceZoneTemplateResponseDto> updateAudienceZoneTemplate(
            @PathVariable Long structureId, @PathVariable Long areaId, @PathVariable Long templateId, @Valid @RequestBody AudienceZoneTemplateUpdateDto updateDto, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateAudienceZoneTemplate(structureId, areaId, templateId, updateDto));
    }

    @Operation(summary = "Supprimer un modèle de zone",
            description = "Supprime un modèle de zone d'audience. Nécessite d'être propriétaire.")
    @DeleteMapping("/{structureId}/areas/{areaId}/audience-zone-templates/{templateId}")
    @PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
    public ResponseEntity<Void> deleteAudienceZoneTemplate(@PathVariable Long structureId, @PathVariable Long areaId, @PathVariable Long templateId, Authentication authentication) {
        structureService.deleteAudienceZoneTemplate(structureId, areaId, templateId);
        return ResponseEntity.noContent().build();
    }
}