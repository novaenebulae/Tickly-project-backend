package edu.cda.project.ticklybackend.repositories.ticket;

import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
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
     * Trouve tous les billets valides pour un événement spécifique.
     *
     * @param eventId L'ID de l'événement.
     * @return une liste de billets valides pour l'événement.
     */
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'VALID'")
    List<Ticket> findValidTicketsByEventId(@Param("eventId") Long eventId);

    /**
     * Compte le nombre de billets existants pour une zone d'audience spécifique d'un événement.
     *
     * @param zoneId L'ID de la zone d'audience de l'événement.
     * @return le nombre de billets.
     */
    long countByEventAudienceZoneId(Long zoneId);

    /**
     * Compte le nombre de billets existants pour une zone d'audience spécifique d'un événement
     * avec un statut spécifique.
     *
     * @param zone   La zone d'audience de l'événement.
     * @param status Le statut des billets à compter.
     * @return le nombre de billets.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.eventAudienceZone = :zone AND t.status = :status")
    Long countByEventAudienceZoneAndStatus(@Param("zone") EventAudienceZone zone, @Param("status") TicketStatus status);

    /**
     * Compte le nombre de billets existants pour une zone d'audience spécifique d'un événement
     * avec un des statuts spécifiés.
     *
     * @param zoneId   L'ID de la zone d'audience de l'événement.
     * @param statuses Les statuts des billets à compter.
     * @return le nombre de billets.
     */
    long countByEventAudienceZoneIdAndStatusIn(Long zoneId, Collection<TicketStatus> statuses);


    List<Ticket> findAllByEventId(Long eventId);


    long countByEventStructureIdAndStatusIn(Long structureId, Collection<TicketStatus> statuses);

    long countByEventStructureIdAndEventStartDateAfterAndStatus(Long structureId, Instant startDate, TicketStatus status);

    long countByEventIdAndStatus(Long eventId, TicketStatus status);

    long countByEventIdAndStatusIn(Long eventId, Collection<TicketStatus> statuses);

}
