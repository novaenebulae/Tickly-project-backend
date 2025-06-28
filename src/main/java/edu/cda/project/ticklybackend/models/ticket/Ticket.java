package edu.cda.project.ticklybackend.models.ticket;

import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente un billet unique pour un événement.
 * La clé primaire est un UUID pour garantir une unicité globale et des identifiants non prédictibles.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_audience_zone_id", nullable = false)
    private EventAudienceZone eventAudienceZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // L'utilisateur qui a acheté le billet

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @NotNull
    @Column(name = "qr_code_value", unique = true, nullable = false)
    private String qrCodeValue;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "participant_first_name", nullable = false)
    private String participantFirstName;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "participant_last_name", nullable = false)
    private String participantLastName;

    @Email
    @Column(name = "participant_email", nullable = false)
    private String participantEmail;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.VALID;

    @NotNull
    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

//    @Column(precision = 10, scale = 2)
//    private BigDecimal price;

//    @Size(max = 50)
//    @Column(name = "seat_number", length = 50)
//    private String seatNumber;

    @PrePersist
    protected void onCreate() {
        this.reservationDate = LocalDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.qrCodeValue == null) {
            this.qrCodeValue = UUID.randomUUID().toString();
        }
    }
}