package edu.cda.project.ticklybackend.security;


import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Service pour gérer la logique de sécurité spécifique aux billets (tickets).
 * Utilisé dans les annotations @PreAuthorize pour des vérifications d'autorisation fines.
 */
@Service("ticketSecurityService") // Nom du bean qui sera utilisé dans les expressions SpEL
@RequiredArgsConstructor
@Slf4j
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

    /**
     * Vérifie si l'utilisateur authentifié a le droit de valider un billet.
     * Pour valider un billet, l'utilisateur doit faire partie du personnel de la structure
     * qui a créé l'événement associé au billet.
     *
     * @param qrCodeValue    La valeur du QR code du billet à valider.
     * @param authentication L'objet Authentication de l'utilisateur authentifié.
     * @return true si l'utilisateur peut valider le billet, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean canValidateTicket(String qrCodeValue, Authentication authentication) {
        if (qrCodeValue == null || qrCodeValue.isBlank() || authentication == null) {
            log.warn("Tentative de validation de billet avec des paramètres invalides");
            return false;
        }

        return ticketRepository.findByQrCodeValue(qrCodeValue)
                .map(ticket -> {
                    Long structureId = ticket.getEvent().getStructure().getId();
                    log.info("Vérification des permissions pour valider le billet {} de l'événement {} (structure {})",
                            ticket.getId(), ticket.getEvent().getName(), structureId);

                    // Vérification du personnel de la structure
                    boolean isStaffMember = structureSecurityService.isStructureStaff(structureId, authentication);

                    if (!isStaffMember) {
                        log.warn("L'utilisateur n'est pas membre du personnel de la structure {}", structureId);
                        return false;
                    }

                    // Vérification de la fenêtre de temps valide (1h avant le début jusqu'à la fin de l'événement)
                    Instant now = Instant.now();
                    Instant eventStart = ticket.getEvent().getStartDate();
                    Instant eventEnd = ticket.getEvent().getEndDate();
                    Instant validationWindowStart = eventStart.minus(Duration.ofHours(1));

                    boolean isInValidTimeWindow = now.isAfter(validationWindowStart) && now.isBefore(eventEnd);

                    if (!isInValidTimeWindow) {
                        log.warn("Tentative de validation du billet {} en dehors de la fenêtre de temps valide. " +
                                "Heure actuelle: {}, Début de l'événement: {}, Fin de l'événement: {}", 
                                ticket.getId(), now, eventStart, eventEnd);
                        return false;
                    }

                    log.info("Validation du billet {} autorisée", ticket.getId());
                    return true;
                })
                .orElse(false);
    }

}
