package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.common.ErrorResponseDto;
import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationResponseDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Management", description = "API for creating, searching, and managing events.")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    @Operation(
            summary = "Create a new event",
            description = "Creates a new event associated with a structure. Requires a structure administrator or organization service role.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Event created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Structure or Category not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.canCreateInStructure(principal, #creationDto.structureId)")
    public ResponseEntity<EventDetailResponseDto> createEvent(@Valid @RequestBody EventCreationDto creationDto) {
        LoggingUtils.logMethodEntry(log, "createEvent", "structureId", creationDto.getStructureId());
        try {
            EventDetailResponseDto createdEvent = eventService.createEvent(creationDto);
            LoggingUtils.logMethodExit(log, "createEvent", createdEvent);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error creating event for structure ID " + creationDto.getStructureId(), e);
            throw e;
        }
    }

    @Operation(
            summary = "List and search events",
            description = "Retrieves a paginated list of events, with filtering and sorting options. Publicly accessible.",
            responses = @ApiResponse(responseCode = "200", description = "Event list retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponseDto.class)))
    )
    @GetMapping("/events")
    public ResponseEntity<PaginatedResponseDto<EventSummaryDto>> searchEvents(
            @ParameterObject EventSearchParamsDto params,
            @ParameterObject Pageable pageable) {
        LoggingUtils.logMethodEntry(log, "searchEvents", "params", params, "pageable", pageable);
        try {
            PaginatedResponseDto<EventSummaryDto> result = eventService.searchEvents(params, pageable);
            LoggingUtils.logMethodExit(log, "searchEvents", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error searching for events", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get event details by ID",
            description = "Publicly accessible.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDetailResponseDto> getEventById(@Parameter(description = "ID of the event to retrieve") @PathVariable Long eventId) {
        LoggingUtils.logMethodEntry(log, "getEventById", "eventId", eventId);
        try {
            EventDetailResponseDto event = eventService.getEventById(eventId);
            LoggingUtils.logMethodExit(log, "getEventById", event);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error retrieving details for event ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Get friends attending an event",
            description = "Returns the list of friends of the connected user who are attending the specified event. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of attending friends", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/events/{eventId}/friends")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FriendResponseDto>> getFriendsAttendingEvent(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        LoggingUtils.logMethodEntry(log, "getFriendsAttendingEvent", "eventId", eventId);
        try {
            List<FriendResponseDto> friendsAttending = eventService.getFriendsAttendingEvent(eventId);
            LoggingUtils.logMethodExit(log, "getFriendsAttendingEvent", friendsAttending);
            return ResponseEntity.ok(friendsAttending);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error retrieving friends attending event ID " + eventId, e);
            throw e;
        }
    }


    @Operation(
            summary = "Partially update an existing event",
            description = "Updates event information. Only the provided fields will be modified. Only the event owner can perform this action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    @PatchMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<EventDetailResponseDto> updateEvent(
            @Parameter(description = "ID of the event to update") @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateDto updateDto) {
        LoggingUtils.logMethodEntry(log, "updateEvent", "eventId", eventId, "updateDto", updateDto);
        try {
            EventDetailResponseDto updatedEvent = eventService.updateEvent(eventId, updateDto);
            LoggingUtils.logMethodExit(log, "updateEvent", updatedEvent);
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error updating event ID " + eventId, e);
            throw e;
        }
    }


    @Operation(
            summary = "Delete an event",
            description = "Deletes an event and all associated files. Irreversible operation. Only the owner can perform this action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Event successfully deleted"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<Void> deleteEvent(@Parameter(description = "ID of the event to delete") @PathVariable Long eventId) {
        LoggingUtils.logMethodEntry(log, "deleteEvent", "eventId", eventId);
        try {
            eventService.deleteEvent(eventId);
            LoggingUtils.logMethodExit(log, "deleteEvent");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error deleting event ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Update event status",
            description = "Allows changing the status of an event (e.g., from DRAFT to PUBLISHED). Only the owner can perform this action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid status"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    @PatchMapping("/events/{eventId}/status")
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<EventDetailResponseDto> updateEventStatus(@Parameter(description = "Event ID") @PathVariable Long eventId, @Valid @RequestBody EventStatusUpdateDto statusUpdateDto) {
        LoggingUtils.logMethodEntry(log, "updateEventStatus", "eventId", eventId, "status", statusUpdateDto.getStatus());
        try {
            EventDetailResponseDto updatedEvent = eventService.updateEventStatus(eventId, statusUpdateDto);
            LoggingUtils.logMethodExit(log, "updateEventStatus", updatedEvent);
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error updating status for event ID " + eventId, e);
            throw e;
        }
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
        LoggingUtils.logMethodEntry(log, "uploadMainPhoto", "eventId", eventId, "fileName", file.getOriginalFilename());
        try {
            String fileUrl = eventService.updateEventMainPhoto(eventId, file);
            FileUploadResponseDto response = new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Photo principale mise à jour.");
            LoggingUtils.logMethodExit(log, "uploadMainPhoto", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'upload de la photo principale pour l'événement ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Ajouter des images à la galerie d'un événement",
            description = "Ajoute de nouvelles images à la galerie.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", description = "Image ajoutée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponseDto.class)))
    )
    @PostMapping(value = "/events/{eventId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STRUCTURE_ADMINISTRATOR', 'ORGANIZATION_SERVICE') and @eventSecurityService.isOwner(#eventId, principal)")
    public ResponseEntity<List<FileUploadResponseDto>> addGalleryImages(
            @PathVariable Long eventId,
            @RequestParam("files") MultipartFile[] files) {
        LoggingUtils.logMethodEntry(log, "addGalleryImages", "eventId", eventId, "filesCount", files.length);
        try {
            List<FileUploadResponseDto> responses = eventService.addEventGalleryImages(eventId, files);
            LoggingUtils.logMethodExit(log, "addGalleryImages", responses);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'ajout d'images à la galerie de l'événement ID " + eventId, e);
            throw e;
        }
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
        LoggingUtils.logMethodEntry(log, "removeGalleryImage", "eventId", eventId, "imagePath", imagePath);
        try {
            eventService.removeEventGalleryImage(eventId, imagePath);
            LoggingUtils.logMethodExit(log, "removeGalleryImage");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression de l'image '" + imagePath + "' de la galerie de l'événement ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Récupérer toutes les catégories d'événements disponibles",
            description = "Accessible publiquement.",
            responses = @ApiResponse(responseCode = "200", description = "Liste des catégories", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    )
    @GetMapping("/event-categories")
    public ResponseEntity<List<EventCategoryDto>> getAllCategories() {
        LoggingUtils.logMethodEntry(log, "getAllCategories");
        try {
            List<EventCategoryDto> categories = eventService.getAllCategories();
            LoggingUtils.logMethodExit(log, "getAllCategories", categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des catégories d'événements", e);
            throw e;
        }
    }

    @Operation(
            summary = "Récupérer les billets d'un événement pour la gestion",
            description = "Récupère une liste paginée et filtrée de tous les billets pour un événement spécifique, conçue pour la gestion par le personnel.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des billets récupérée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Événement non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/events/{eventId}/management/tickets")
    @PreAuthorize("@ticketSecurityService.canValidateEventTickets(#eventId, authentication)")
    public ResponseEntity<PaginatedResponseDto<TicketResponseDto>> getEventTickets(
            @Parameter(description = "ID de l'événement") @PathVariable Long eventId,
            @Parameter(description = "Statut des billets à filtrer (optionnel)") @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Terme de recherche pour filtrer les billets (optionnel)") @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        LoggingUtils.logMethodEntry(log, "getEventTickets", "eventId", eventId, "status", status, "search", search, "pageable", pageable);
        try {
            PaginatedResponseDto<TicketResponseDto> tickets = ticketService.getEventTickets(eventId, status, search, pageable);
            LoggingUtils.logMethodExit(log, "getEventTickets", tickets);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des billets pour l'événement ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Valider un billet",
            description = "Marque un billet spécifique comme UTILISÉ. Cet endpoint est conçu pour la validation manuelle depuis le panneau du personnel.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Billet validé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketValidationResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Billet non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @PostMapping("/events/{eventId}/management/tickets/{ticketId}/validate")
    @PreAuthorize("@ticketSecurityService.canValidateEventTickets(#eventId, authentication)")
    public ResponseEntity<TicketValidationResponseDto> validateTicket(
            @Parameter(description = "ID de l'événement") @PathVariable Long eventId,
            @Parameter(description = "ID du billet à valider") @PathVariable UUID ticketId) {
        LoggingUtils.logMethodEntry(log, "validateTicket", "eventId", eventId, "ticketId", ticketId);
        try {
            TicketValidationResponseDto result = ticketService.validateTicket(ticketId);
            LoggingUtils.logMethodExit(log, "validateTicket", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la validation du billet ID " + ticketId + " pour l'événement ID " + eventId, e);
            throw e;
        }
    }
}
