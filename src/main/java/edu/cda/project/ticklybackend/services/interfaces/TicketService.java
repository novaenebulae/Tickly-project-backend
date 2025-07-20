package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.ticket.ReservationConfirmationDto;
import edu.cda.project.ticklybackend.dtos.ticket.ReservationRequestDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationResponseDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import org.springframework.data.domain.Pageable;
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
     * Récupère une liste paginée de billets pour un événement spécifique.
     * Permet de filtrer par statut et de rechercher par nom, email ou UUID du billet.
     *
     * @param eventId L'ID de l'événement.
     * @param status  Le statut des billets à filtrer (optionnel).
     * @param search  Terme de recherche pour filtrer les billets (optionnel).
     * @param pageable Informations de pagination.
     * @return une réponse paginée contenant les détails des billets.
     */
    PaginatedResponseDto<TicketResponseDto> getEventTickets(Long eventId, TicketStatus status, String search, Pageable pageable);

    /**
     * Valide un billet spécifique en changeant son statut à USED.
     *
     * @param ticketId L'UUID du billet à valider.
     * @return un DTO contenant le résultat de la validation.
     */
    TicketValidationResponseDto validateTicket(UUID ticketId);

}
