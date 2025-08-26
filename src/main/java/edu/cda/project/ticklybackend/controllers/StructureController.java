package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.common.ErrorResponseDto;
import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

/**
 * Authenticated endpoints to manage structures and nested resources:
 * areas and audience zone templates, including media operations.
 */
@RestController
@RequestMapping("/api/v1/structures")
@RequiredArgsConstructor
@Tag(name = "Structures", description = "API for managing event structures and their components.")
@SecurityRequirement(name = "bearerAuth")
public class StructureController {

    private final StructureService structureService;


    @Operation(summary = "Create a new structure",
            description = "Allows a structure administrator (with 'needsStructureSetup' set to true) to create their structure. " +
                    "The user's JWT token will be implicitly updated on the server side to reflect the association.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Structure successfully created", content = @Content(schema = @Schema(implementation = StructureCreationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid creation data", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not authorized to create a structure or already has one")
    })
    @PostMapping
    @PreAuthorize("@organizationalSecurityService.canCreateStructure(authentication)")
    public ResponseEntity<StructureCreationResponseDto> createStructure(
            @Parameter(description = "DTO for structure creation", required = true)
            @Valid @RequestBody StructureCreationDto creationDto,
            Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        StructureCreationResponseDto responseDto = structureService.createStructure(creationDto, authenticatedUser.getEmail());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "List all structures",
            description = "Retrieves a paginated and potentially filtered list of all structures. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "List of structures successfully retrieved", content = @Content(schema = @Schema(implementation = Page.class)))
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<StructureSummaryDto>> getAllStructures(
            @ParameterObject Pageable pageable,
            @Parameter(description = "Optional filters Ex: city=Paris&typeId=1")
            @ParameterObject StructureSearchParamsDto params) {
        Page<StructureSummaryDto> structures = structureService.getAllStructures(pageable, params);
        return ResponseEntity.ok(structures);
    }

    @Operation(summary = "Get structure details",
            description = "Retrieves detailed information about a structure by its ID. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Structure details", content = @Content(schema = @Schema(implementation = StructureDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Structure not found", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{structureId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<StructureDetailResponseDto> getStructureById(@PathVariable Long structureId) {
        return ResponseEntity.ok(structureService.getStructureById(structureId));
    }


    @Operation(summary = "Update a structure (partial)",
            description = "Partially updates an existing structure. Only the provided fields will be modified. " +
                    "Accessible to system administrators or the structure owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Structure successfully updated", content = @Content(schema = @Schema(implementation = StructureDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Structure not found")
    })
    @PatchMapping("/{structureId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMINISTRATOR') or @organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<StructureDetailResponseDto> updateStructure(
            @PathVariable Long structureId,
            @Valid @RequestBody StructureUpdateDto updateDto,
            Authentication authentication) {
        return ResponseEntity.ok(structureService.updateStructure(structureId, updateDto));
    }

    @Operation(summary = "Delete a structure",
            description = "Deletes a structure and all its associated components and files. " +
                    "Accessible to system administrators or the structure owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Structure successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Structure not found")
    })
    @DeleteMapping("/{structureId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMINISTRATOR') or @organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long structureId, Authentication authentication) {
        structureService.deleteStructure(structureId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Update a structure's logo",
            description = "Replaces the current logo of the structure. Requires being the owner.")
    @PostMapping(value = "/{structureId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<FileUploadResponseDto> updateStructureLogo(@PathVariable Long structureId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateStructureLogo(structureId, file));
    }

    @Operation(summary = "Delete a structure's logo",
            description = "Removes the current logo of the structure. Requires being the owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logo successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Structure not found or logo does not exist")
    })
    @DeleteMapping("/{structureId}/logo")
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<Void> removeStructureLogo(@PathVariable Long structureId, Authentication authentication) {
        structureService.removeStructureLogo(structureId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a structure's cover image",
            description = "Replaces the current cover image of the structure. Requires being the owner.")
    @PostMapping(value = "/{structureId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<FileUploadResponseDto> updateStructureCover(@PathVariable Long structureId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateStructureCover(structureId, file));
    }

    @Operation(summary = "Delete a structure's cover image",
            description = "Removes the current cover image of the structure. Requires being the owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cover image successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Structure not found or cover image does not exist")
    })
    @DeleteMapping("/{structureId}/cover")
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<Void> removeStructureCover(@PathVariable Long structureId, Authentication authentication) {
        structureService.removeStructureCover(structureId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add multiple images to a structure's gallery",
            description = "Adds multiple new images to the structure's gallery in a single operation.")
    @PostMapping(value = "/{structureId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<List<FileUploadResponseDto>> addMultipleStructureGalleryImages(
            @PathVariable Long structureId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {

        List<FileUploadResponseDto> results = structureService.addStructureGalleryImages(structureId, files);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Remove an image from a structure's gallery",
            description = "Deletes a specific image from the gallery. Requires being the owner.")
    @DeleteMapping("/{structureId}/gallery")
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<Void> removeStructureGalleryImage(
            @PathVariable Long structureId,
            @Parameter(description = "Path of the image to delete, as returned in the gallery URLs.", required = true)
            @RequestParam String imagePath,
            Authentication authentication) {
        structureService.removeStructureGalleryImage(structureId, imagePath);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all available structure types",
            description = "Returns a list of all structure types that event venues can adopt.")
    @ApiResponse(responseCode = "200", description = "List of structure types successfully retrieved.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = StructureTypeDto.class))))
    @GetMapping("/types")
    public ResponseEntity<List<StructureTypeDto>> getAllStructureTypes() {
        return ResponseEntity.ok(structureService.getAllStructureTypes());
    }


    @Operation(summary = "Create an area for a structure",
            description = "Adds a new physical space (room, stage...) to a structure. Requires being the owner.")
    @PostMapping("/{structureId}/areas")
    @PreAuthorize("@organizationalSecurityService.canModifyStructure(#structureId, authentication)")
    public ResponseEntity<AreaResponseDto> createArea(@PathVariable Long structureId, @Valid @RequestBody AreaCreationDto creationDto, Authentication authentication) {
        return new ResponseEntity<>(structureService.createArea(structureId, creationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "List areas of a structure",
            description = "Retrieves all physical spaces configured for a given structure. Accessible to the owner or organization services.")
    @GetMapping("/{structureId}/areas")
    @PreAuthorize("@organizationalSecurityService.canAccessStructure(#structureId, authentication)")
    public ResponseEntity<List<AreaResponseDto>> getAreasByStructureId(@PathVariable Long structureId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAreasByStructureId(structureId));
    }

    @Operation(summary = "Get a specific area",
            description = "Retrieves the details of an area by its ID, in the context of a structure.")
    @GetMapping("/{structureId}/areas/{areaId}")
    @PreAuthorize("@organizationalSecurityService.canAccessStructure(#structureId, authentication)")
    public ResponseEntity<AreaResponseDto> getAreaById(@PathVariable Long structureId, @PathVariable Long areaId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAreaById(structureId, areaId));
    }

    @Operation(summary = "Update an area",
            description = "Partially updates an existing area. Requires being the owner of the parent structure.")
    @PatchMapping("/{structureId}/areas/{areaId}")
    @PreAuthorize("@organizationalSecurityService.canModifyStructure(#structureId, authentication)")
    public ResponseEntity<AreaResponseDto> updateArea(@PathVariable Long structureId, @PathVariable Long areaId, @Valid @RequestBody AreaUpdateDto updateDto, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateArea(structureId, areaId, updateDto));
    }

    @Operation(summary = "Delete an area",
            description = "Removes an area from a structure. Requires being the owner.")
    @DeleteMapping("/{structureId}/areas/{areaId}")
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<Void> deleteArea(@PathVariable Long structureId, @PathVariable Long areaId, Authentication authentication) {
        structureService.deleteArea(structureId, areaId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Create an audience zone template",
            description = "Adds a new zone template (pit, balcony...) to a specific area. Requires being the owner of the structure.")
    @PostMapping("/{structureId}/areas/{areaId}/audience-zone-templates")
    @PreAuthorize("@organizationalSecurityService.canModifyStructure(#structureId, authentication)")
    public ResponseEntity<AudienceZoneTemplateResponseDto> createAudienceZoneTemplate(
            @PathVariable Long structureId, @PathVariable Long areaId, @Valid @RequestBody AudienceZoneTemplateCreationDto creationDto, Authentication authentication) {
        return new ResponseEntity<>(structureService.createAudienceZoneTemplate(structureId, areaId, creationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "List audience zone templates for an area",
            description = "Retrieves all audience zone templates for a given area.")
    @GetMapping("/{structureId}/areas/{areaId}/audience-zone-templates")
    @PreAuthorize("@organizationalSecurityService.canAccessStructure(#structureId, authentication)")
    public ResponseEntity<List<AudienceZoneTemplateResponseDto>> getAudienceZoneTemplatesByAreaId(@PathVariable Long structureId, @PathVariable Long areaId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAudienceZoneTemplatesByAreaId(structureId, areaId));
    }

    @Operation(summary = "Get a specific audience zone template",
            description = "Retrieves the details of a zone template by its ID.")
    @GetMapping("/{structureId}/areas/{areaId}/audience-zone-templates/{templateId}")
    @PreAuthorize("@organizationalSecurityService.canAccessStructure(#structureId, authentication)")
    public ResponseEntity<AudienceZoneTemplateResponseDto> getAudienceZoneTemplateById(@PathVariable Long structureId, @PathVariable Long areaId, @PathVariable Long templateId, Authentication authentication) {
        return ResponseEntity.ok(structureService.getAudienceZoneTemplateById(structureId, areaId, templateId));
    }

    @Operation(summary = "Update an audience zone template",
            description = "Updates an existing zone template. Requires being the owner.")
    @PatchMapping("/{structureId}/areas/{areaId}/audience-zone-templates/{templateId}")
    @PreAuthorize("@organizationalSecurityService.canModifyStructure(#structureId, authentication)")
    public ResponseEntity<AudienceZoneTemplateResponseDto> updateAudienceZoneTemplate(
            @PathVariable Long structureId, @PathVariable Long areaId, @PathVariable Long templateId, @Valid @RequestBody AudienceZoneTemplateUpdateDto updateDto, Authentication authentication) {
        return ResponseEntity.ok(structureService.updateAudienceZoneTemplate(structureId, areaId, templateId, updateDto));
    }

    @Operation(summary = "Delete an audience zone template",
            description = "Removes an audience zone template. Requires being the owner.")
    @DeleteMapping("/{structureId}/areas/{areaId}/audience-zone-templates/{templateId}")
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<Void> deleteAudienceZoneTemplate(@PathVariable Long structureId, @PathVariable Long areaId, @PathVariable Long templateId, Authentication authentication) {
        structureService.deleteAudienceZoneTemplate(structureId, areaId, templateId);
        return ResponseEntity.noContent().build();
    }
}
