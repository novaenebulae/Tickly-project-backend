package edu.cda.project.ticklybackend.models.team;

import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entité de liaison qui représente l'appartenance d'un utilisateur à une structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private Structure structure;

    /**
     * L'utilisateur lié. Peut être nul si l'invitation n'a pas encore été acceptée.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * L'email de la personne invitée. Utilisé pour faire le lien lors de l'acceptation.
     */
    @Column(nullable = false)
    private String email;

    /**
     * Le rôle de l'utilisateur au sein de cette structure spécifique.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * Le statut de l'adhésion (en attente ou actif).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant invitedAt;

    private Instant joinedAt;
}