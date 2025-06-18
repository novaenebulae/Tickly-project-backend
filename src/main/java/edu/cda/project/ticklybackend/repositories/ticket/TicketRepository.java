package edu.cda.project.ticklybackend.repositories.ticket;

import edu.cda.project.ticklybackend.models.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Compte le nombre de billets existants pour une zone d'audience spécifique d'un événement.
     *
     * @param zoneId L'ID de la zone d'audience de l'événement.
     * @return le nombre de billets.
     */
    long countByEventAudienceZoneId(Long zoneId);
}