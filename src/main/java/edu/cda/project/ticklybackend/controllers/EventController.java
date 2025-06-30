package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.common.ErrorResponseDto;
import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Gestion des Événements", description = "API pour la création, recherche et gestion des événements.")
public class EventController {

    private final EventService eventService;

    @Operation(
            summary = "Créer un nouvel événement",
            description = "Crée un nouvel événement associé à une structure. Nécessite un rôle d'administrateur de structure ou de service d'organisation.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Événement créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Données de requête invalides", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Structure ou Catégorie non trouvée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.canCreateInStructure(principal, #creationDto.structureId)")
    public ResponseEntity<EventDetailResponseDto> createEvent(@Valid @RequestBody EventCreationDto creationDto) {
        EventDetailResponseDto createdEvent = eventService.createEvent(creationDto);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Lister et rechercher des événements",
            description = "Récupère une liste paginée d'événements, avec des options de filtrage et de tri. Accessible publiquement.",
            responses = @ApiResponse(responseCode = "200", description = "Liste des événements récupérée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponseDto.class)))
    )
    @GetMapping("/events")
    public ResponseEntity<PaginatedResponseDto<EventSummaryDto>> searchEvents(
            @ParameterObject EventSearchParamsDto params,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(eventService.searchEvents(params, pageable));
    }

    @Operation(
            summary = "Récupérer les détails d'un événement par son ID",
            description = "Accessible publiquement.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Détails de l'événement", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Événement non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDetailResponseDto> getEventById(@Parameter(description = "ID de l'événement à récupérer") @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @Operation(
            summary = "Récupérer les amis participant à un événement",
            description = "Retourne la liste des amis de l'utilisateur connecté qui participent à l'événement spécifié. Nécessite une authentification.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des amis participants", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Événement non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/events/{eventId}/friends")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FriendResponseDto>> getFriendsAttendingEvent(
            @Parameter(description = "ID de l'événement") @PathVariable Long eventId) {
        List<FriendResponseDto> friendsAttending = eventService.getFriendsAttendingEvent(eventId);
        return ResponseEntity.ok(friendsAttending);
    }


    @Operation(
            summary = "Mettre à jour un événement existant",
            description = "Met à jour les informations d'un événement. Seul le propriétaire de l'événement (via sa structure) peut effectuer cette action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Événement mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé"),
                    @ApiResponse(responseCode = "404", description = "Événement non trouvé")
            }
    )
    @PutMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<EventDetailResponseDto> updateEvent(@Parameter(description = "ID de l'événement à mettre à jour") @PathVariable Long eventId, @Valid @RequestBody EventUpdateDto updateDto) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, updateDto));
    }

    @Operation(
            summary = "Supprimer un événement",
            description = "Supprime un événement et tous les fichiers associés. Opération irréversible. Seul le propriétaire peut effectuer cette action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Événement supprimé avec succès"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé"),
                    @ApiResponse(responseCode = "404", description = "Événement non trouvé")
            }
    )
    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<Void> deleteEvent(@Parameter(description = "ID de l'événement à supprimer") @PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Mettre à jour le statut d'un événement",
            description = "Permet de changer le statut d'un événement (ex: de DRAFT à PUBLISHED). Seul le propriétaire peut effectuer cette action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statut mis à jour", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Statut invalide"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé"),
                    @ApiResponse(responseCode = "404", description = "Événement non trouvé")
            }
    )
    @PatchMapping("/events/{eventId}/status")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<EventDetailResponseDto> updateEventStatus(@Parameter(description = "ID de l'événement") @PathVariable Long eventId, @Valid @RequestBody EventStatusUpdateDto statusUpdateDto) {
        return ResponseEntity.ok(eventService.updateEventStatus(eventId, statusUpdateDto));
    }

    @Operation(
            summary = "Uploader ou mettre à jour la photo principale d'un événement",
            description = "Remplace la photo principale existante. Seul le propriétaire peut effectuer cette action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", description = "Photo mise à jour", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponseDto.class)))
    )
    @PostMapping(value = "/events/{eventId}/main-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<FileUploadResponseDto> uploadMainPhoto(
            @Parameter(description = "ID de l'événement") @PathVariable Long eventId,
            @Parameter(description = "Fichier image à uploader") @RequestParam("file") MultipartFile file) {
        String fileUrl = eventService.updateEventMainPhoto(eventId, file);
        return ResponseEntity.ok(new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Photo principale mise à jour."));
    }

    @Operation(
            summary = "Ajouter une image à la galerie d'un événement",
            description = "Ajoute une nouvelle image à la galerie. Seul le propriétaire peut effectuer cette action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", description = "Image ajoutée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponseDto.class)))
    )
    @PostMapping(value = "/events/{eventId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<FileUploadResponseDto> addGalleryImage(
            @Parameter(description = "ID de l'événement") @PathVariable Long eventId,
            @Parameter(description = "Fichier image à ajouter") @RequestParam("file") MultipartFile file) {
        String fileUrl = eventService.addEventGalleryImage(eventId, file);
        return ResponseEntity.ok(new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Image ajoutée à la galerie."));
    }

    @Operation(
            summary = "Supprimer une image de la galerie d'un événement",
            description = "Supprime une image spécifique de la galerie. Seul le propriétaire peut effectuer cette action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "204", description = "Image supprimée")
    )
    @DeleteMapping("/events/{eventId}/gallery")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<Void> removeGalleryImage(
            @Parameter(description = "ID de l'événement") @PathVariable Long eventId,
            @Parameter(description = "Chemin/nom du fichier image à supprimer (tel que retourné par l'API)") @RequestParam String imagePath) {
        eventService.removeEventGalleryImage(eventId, imagePath);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Récupérer toutes les catégories d'événements disponibles",
            description = "Accessible publiquement.",
            responses = @ApiResponse(responseCode = "200", description = "Liste des catégories", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    )
    @GetMapping("/event-categories")
    public ResponseEntity<List<EventCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(eventService.getAllCategories());
    }
}
