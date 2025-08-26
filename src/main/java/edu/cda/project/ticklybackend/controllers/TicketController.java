package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.ticket.ReservationConfirmationDto;
import edu.cda.project.ticklybackend.dtos.ticket.ReservationRequestDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Ticketing endpoints for creating/canceling reservations and retrieving
 * ticket details for both authenticated and public flows.
 */
@RestController
@RequestMapping("/api/v1/ticketing")
@Tag(name = "Ticketing API", description = "Endpoints for ticket reservation, viewing, and validation.")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new reservation", description = "Creates a new reservation for one or more tickets for a specific event.")
    public ResponseEntity<ReservationConfirmationDto> createReservation(@Valid @RequestBody ReservationRequestDto requestDto) {
        ReservationConfirmationDto confirmation = ticketService.createReservation(requestDto);
        return new ResponseEntity<>(confirmation, HttpStatus.CREATED);
    }

    @GetMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my reservations", description = "Retrieves a list of all reservations made by the authenticated user.")
    public ResponseEntity<List<ReservationConfirmationDto>> getMyReservations() {
        List<ReservationConfirmationDto> tickets = ticketService.getMyReservations();
        return ResponseEntity.ok(tickets);
    }


    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("@organizationalSecurityService.isTicketOwner(#ticketId, authentication)")
    @Operation(summary = "Get ticket details (authenticated)", description = "Retrieves the details of a specific ticket. The user must be the owner of the ticket.")
    public ResponseEntity<TicketResponseDto> getTicketDetails(@PathVariable UUID ticketId) {
        TicketResponseDto ticket = ticketService.getTicketDetails(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/public/tickets/{ticketId}")
    @Operation(summary = "Get ticket details by UUID (public)",
            description = "Retrieves the details of a specific ticket using its UUID. " +
                    "This endpoint is public and does not require authentication. " +
                    "Security is ensured by the ticket's UUID which is difficult to guess.")
    public ResponseEntity<TicketResponseDto> getPublicTicketDetails(@PathVariable UUID ticketId) {
        TicketResponseDto ticket = ticketService.getPublicTicketDetails(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/reservations/{reservationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a reservation",
            description = "Cancels a reservation and all associated tickets. " +
                    "Canceled tickets free up spaces for the event. " +
                    "Only the owner of the reservation can cancel it.")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        boolean success = ticketService.cancelReservation(reservationId);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
