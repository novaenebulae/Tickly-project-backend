package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entité représentant une relation d'amitié entre deux utilisateurs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendships", uniqueConstraints = {
        // Assure qu'une paire (émetteur, destinataire) est unique.
        @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [cite_start]L'utilisateur qui a envoyé la demande d'ami. [cite: 1612]
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * [cite_start]L'utilisateur qui a reçu la demande d'ami. [cite: 1612]
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * [cite_start]Le statut actuel de la relation. [cite: 1612]
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}