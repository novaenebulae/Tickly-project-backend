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

/**
 * REST endpoints for public event discovery and authenticated event management.
 * Includes creation, updates, media management, ticket administration, and validation.
 */
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
    @PreAuthorize("@organizationalSecurityService.canModifyStructure(#creationDto.structureId, authentication)")
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
    @PreAuthorize("@organizationalSecurityService.canModifyEvent(#eventId, authentication)")
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
    @PreAuthorize("@organizationalSecurityService.canModifyEvent(#eventId, authentication)")
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
    @PreAuthorize("@organizationalSecurityService.canModifyEvent(#eventId, authentication)")
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
            summary = "Upload or replace an event's main photo",
            description = "Replaces the current main photo of the event. Only the event owner can perform this action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", description = "Main photo updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponseDto.class)))
    )
    @PostMapping(value = "/events/{eventId}/main-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@organizationalSecurityService.canModifyEvent(#eventId, authentication)")
    public ResponseEntity<FileUploadResponseDto> uploadMainPhoto(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file) {
        LoggingUtils.logMethodEntry(log, "uploadMainPhoto", "eventId", eventId, "fileName", file.getOriginalFilename());
        try {
            String fileUrl = eventService.updateEventMainPhoto(eventId, file);
            FileUploadResponseDto response = new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Photo principale mise à jour.");
            LoggingUtils.logMethodExit(log, "uploadMainPhoto", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error while uploading main photo for event ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Add images to an event's gallery",
            description = "Adds one or more images to the event's gallery.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", description = "Images added", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponseDto.class)))
    )
    @PostMapping(value = "/events/{eventId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@organizationalSecurityService.canModifyEvent(#eventId, authentication)")
    public ResponseEntity<List<FileUploadResponseDto>> addGalleryImages(
            @PathVariable Long eventId,
            @RequestParam("files") MultipartFile[] files) {
        LoggingUtils.logMethodEntry(log, "addGalleryImages", "eventId", eventId, "filesCount", files.length);
        try {
            List<FileUploadResponseDto> responses = eventService.addEventGalleryImages(eventId, files);
            LoggingUtils.logMethodExit(log, "addGalleryImages", responses);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error while adding images to the gallery for event ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Remove an image from an event's gallery",
            description = "Deletes a specific image from the gallery. Only the event owner can perform this action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "204", description = "Image deleted")
    )
    @DeleteMapping("/events/{eventId}/gallery")
    @PreAuthorize("@organizationalSecurityService.canModifyEvent(#eventId, authentication)")
    public ResponseEntity<Void> removeGalleryImage(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Parameter(description = "Path/name of the image file to delete (as returned by the API)") @RequestParam String imagePath) {
        LoggingUtils.logMethodEntry(log, "removeGalleryImage", "eventId", eventId, "imagePath", imagePath);
        try {
            eventService.removeEventGalleryImage(eventId, imagePath);
            LoggingUtils.logMethodExit(log, "removeGalleryImage");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error while deleting image '" + imagePath + "' from the gallery of event ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Get all available event categories",
            description = "Publicly accessible.",
            responses = @ApiResponse(responseCode = "200", description = "List of categories", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    )
    @GetMapping("/event-categories")
    public ResponseEntity<List<EventCategoryDto>> getAllCategories() {
        LoggingUtils.logMethodEntry(log, "getAllCategories");
        try {
            List<EventCategoryDto> categories = eventService.getAllCategories();
            LoggingUtils.logMethodExit(log, "getAllCategories", categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error retrieving event categories", e);
            throw e;
        }
    }

    @Operation(
            summary = "Retrieve event tickets for management",
            description = "Returns a paginated and filterable list of all tickets for a specific event, intended for staff management.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ticket list retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/events/{eventId}/management/tickets")
    @PreAuthorize("@organizationalSecurityService.canValidateEventTickets(#eventId, authentication)")
    public ResponseEntity<PaginatedResponseDto<TicketResponseDto>> getEventTickets(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Parameter(description = "Ticket status to filter by (optional)") @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Search term to filter tickets (optional)") @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        LoggingUtils.logMethodEntry(log, "getEventTickets", "eventId", eventId, "status", status, "search", search, "pageable", pageable);
        try {
            PaginatedResponseDto<TicketResponseDto> tickets = ticketService.getEventTickets(eventId, status, search, pageable);
            LoggingUtils.logMethodExit(log, "getEventTickets", tickets);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error retrieving tickets for event ID " + eventId, e);
            throw e;
        }
    }

    @Operation(
            summary = "Validate a ticket",
            description = "Marks a specific ticket as USED. This endpoint is intended for manual validation from the staff panel.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ticket validated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketValidationResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @PostMapping("/events/{eventId}/management/tickets/{ticketId}/validate")
    @PreAuthorize("@organizationalSecurityService.canValidateEventTickets(#eventId, authentication)")
    public ResponseEntity<TicketValidationResponseDto> validateTicket(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Parameter(description = "ID of the ticket to validate") @PathVariable UUID ticketId) {
        LoggingUtils.logMethodEntry(log, "validateTicket", "eventId", eventId, "ticketId", ticketId);
        try {
            TicketValidationResponseDto result = ticketService.validateTicket(ticketId);
            LoggingUtils.logMethodExit(log, "validateTicket", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error validating ticket ID " + ticketId + " for event ID " + eventId, e);
            throw e;
        }
    }
}
