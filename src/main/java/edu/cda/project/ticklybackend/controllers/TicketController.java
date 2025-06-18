package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.ticket.*;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ticketing")
@Tag(name = "API de Billetterie", description = "Endpoints pour la réservation, la consultation et la validation des billets.")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer une nouvelle réservation", description = "Crée une nouvelle réservation pour un ou plusieurs billets pour un événement spécifique.")
    public ResponseEntity<ReservationConfirmationDto> createReservation(@Valid @RequestBody ReservationRequestDto requestDto) {
        ReservationConfirmationDto confirmation = ticketService.createReservation(requestDto);
        return new ResponseEntity<>(confirmation, HttpStatus.CREATED);
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtenir mes billets", description = "Récupère une liste de tous les billets achetés par l'utilisateur authentifié.")
    public ResponseEntity<List<TicketResponseDto>> getMyTickets() {
        List<TicketResponseDto> tickets = ticketService.getMyTickets();
        return ResponseEntity.ok(tickets);
    }


    @GetMapping("/tickets/{ticketId}")
    // L'utilisateur doit être le propriétaire du billet
    @PreAuthorize("@ticketSecurityService.isTicketOwner(#ticketId, authentication.principal)")
    @Operation(summary = "Obtenir les détails d'un billet", description = "Récupère les détails d'un billet spécifique. L'utilisateur doit être le propriétaire du billet.")
    public ResponseEntity<TicketResponseDto> getTicketDetails(@PathVariable UUID ticketId) {
        TicketResponseDto ticket = ticketService.getTicketDetails(ticketId);
        return ResponseEntity.ok(ticket);
    }

    // TODO changer autorisations
    @PostMapping("/tickets/validate")
    @PreAuthorize("hasAnyRole('RESERVATION_SERVICE', 'ORGANIZATION_SERVICE', 'STRUCTURE_ADMINISTRATOR')")
    @Operation(summary = "Valider un billet", description = "Valide un billet en utilisant la valeur scannée de son QR code. Nécessite des rôles de service spécifiques.")
    public ResponseEntity<TicketValidationResponseDto> validateTicket(@Valid @RequestBody TicketValidationRequestDto validationDto) {
        TicketValidationResponseDto response = ticketService.validateTicket(validationDto);
        return ResponseEntity.ok(response);
    }
}
