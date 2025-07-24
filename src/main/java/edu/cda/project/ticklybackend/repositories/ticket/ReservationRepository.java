package edu.cda.project.ticklybackend.repositories.ticket;

import edu.cda.project.ticklybackend.models.ticket.Reservation;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Spring Data JPA pour l'entité Reservation.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByUserId(Long userId);

    Iterable<? extends Ticket> findByUserId(Long userId);
}