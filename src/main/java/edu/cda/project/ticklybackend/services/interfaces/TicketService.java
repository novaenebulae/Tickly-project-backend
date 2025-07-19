package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.ticket.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface TicketService {

    /**
     * Crée une nouvelle réservation avec un ou plusieurs billets.
     *
     * @param requestDto DTO contenant les détails de la réservation.
     * @return un DTO de confirmation avec les détails des billets créés.
     */
    ReservationConfirmationDto createReservation(ReservationRequestDto requestDto);

    /**
     * Récupère tous les billets pour l'utilisateur actuellement authentifié.
     *
     * @return une liste de détails de billets.
     */
    List<TicketResponseDto> getMyTickets();

    /**
     * Récupère les détails d'un billet spécifique par son ID.
     * Vérifie que l'utilisateur authentifié est le propriétaire du billet.
     *
     * @param ticketId L'UUID du billet.
     * @return les détails du billet.
     */
    TicketResponseDto getTicketDetails(UUID ticketId);
    
    /**
     * Récupère les détails d'un billet spécifique par son ID sans vérification d'authentification.
     * Utilisé pour l'accès public aux billets via leur UUID.
     *
     * @param ticketId L'UUID du billet.
     * @return les détails du billet.
     */
    TicketResponseDto getPublicTicketDetails(UUID ticketId);

    /**
     * Valide un billet en utilisant la valeur de son QR code scanné.
     *
     * @param validationDto DTO contenant la valeur du QR code scanné.
     * @return un DTO de réponse avec le résultat de la validation.
     */
    TicketValidationResponseDto validateTicket(TicketValidationRequestDto validationDto);
}
