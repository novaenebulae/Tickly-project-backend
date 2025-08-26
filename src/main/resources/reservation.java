import edu.cda.project.ticklybackend.models.ticket.Ticket;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'entité 'Reservation' et est mappée à la table 'reservations' dans la base de données.
 * Chaque instance groupe plusieurs billets achetés au cours d'une même transaction.
 */
@Data
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Une réservation doit être associée à un utilisateur.")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "reservation")
    private List<Ticket> tickets = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "reservation_date", nullable = false, updatable = false)
    private Instant reservationDate;

}
