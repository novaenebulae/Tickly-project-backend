package edu.cda.project.ticklybackend.repositories.ticket;

import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

/**
 * Repository Spring Data JPA pour l'entité Ticket.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Trouve un billet par la valeur unique de son QR code.
     *
     * @param qrCodeValue La chaîne de caractères du QR code à rechercher.
     * @return un Optional contenant le billet trouvé ou vide sinon.
     */
    Optional<Ticket> findByQrCodeValue(String qrCodeValue);

    /**
     * Trouve tous les billets associés à un ID utilisateur spécifique.
     *
     * @param userId L'ID de l'utilisateur.
     * @return une liste de billets appartenant à l'utilisateur.
     */
    List<Ticket> findByUserId(Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.user.id IN :userIds AND t.status = 'VALID'")
    List<Ticket> findValidTicketsByEventAndUserIds(@Param("eventId") Long eventId, @Param("userIds") Set<Long> userIds);

    /**
     * Compte le nombre de billets existants pour une zone d'audience spécifique d'un événement.
     *
     * @param zoneId L'ID de la zone d'audience de l'événement.
     * @return le nombre de billets.
     */
    long countByEventAudienceZoneId(Long zoneId);
    

    List<Ticket> findAllByEventId(Long eventId);

    // --- Voici les méthodes corrigées ---

    // Spring générera : SELECT COUNT(t) FROM Ticket t WHERE t.event.structure.id = ?1 AND t.status IN (?2)
    long countByEventStructureIdAndStatusIn(Long structureId, Collection<TicketStatus> statuses);

    // Spring générera : SELECT COUNT(t) FROM Ticket t WHERE t.event.structure.id = ?1 AND t.event.startDate > ?2 AND t.status = ?3
    long countByEventStructureIdAndEventStartDateAfterAndStatus(Long structureId, Instant startDate, TicketStatus status);

    // Spring générera : SELECT COUNT(t) FROM Ticket t WHERE t.event.id = ?1 AND t.status = ?2
    long countByEventIdAndStatus(Long eventId, TicketStatus status);

    // Spring générera : SELECT COUNT(t) FROM Ticket t WHERE t.event.id = ?1 AND t.status IN (?2)
    long countByEventIdAndStatusIn(Long eventId, Collection<TicketStatus> statuses);

}
