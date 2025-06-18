package edu.cda.project.ticklybackend.security;


import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Service pour gérer la logique de sécurité spécifique aux billets (tickets).
 * Utilisé dans les annotations @PreAuthorize pour des vérifications d'autorisation fines.
 */
@Service("ticketSecurityService") // Nom du bean qui sera utilisé dans les expressions SpEL
@RequiredArgsConstructor
public class TicketSecurityService {

    private final TicketRepository ticketRepository;
    private final StructureSecurityService structureSecurityService; // On réutilise le service existant

    /**
     * Vérifie si l'utilisateur authentifié est le propriétaire du billet.
     *
     * @param ticketId  L'ID du billet à vérifier.
     * @param principal L'objet UserDetails de l'utilisateur authentifié.
     * @return true si l'utilisateur est le propriétaire, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isTicketOwner(UUID ticketId, UserDetails principal) {
        if (ticketId == null || !(principal instanceof User currentUser)) {
            return false;
        }

        return ticketRepository.findById(ticketId)
                .map(Ticket::getUser)
                .map(User::getId)
                .map(ownerId -> Objects.equals(ownerId, currentUser.getId()))
                .orElse(false);
    }

}