package edu.cda.project.ticklybackend.models.mailing;

import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entité représentant un token à usage unique utilisé pour des opérations sécurisées
 * telles que la validation d'e-mail, la réinitialisation de mot de passe, etc.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    /**
     * Champ flexible pour stocker des données contextuelles au format JSON.
     * Ex: {"teamId": 1, "role": "TEAM_MEMBER"} pour une invitation d'équipe.
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean isUsed = false;

    public VerificationToken(String token, User user, TokenType tokenType, Instant expiryDate, String payload) {
        this.token = token;
        this.user = user;
        this.tokenType = tokenType;
        this.expiryDate = expiryDate;
        this.payload = payload;
        this.isUsed = false;
    }
}