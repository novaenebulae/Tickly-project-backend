package edu.cda.project.ticklybackend.models.mailing;

import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private boolean isUsed = false;

    /**
     * Constructeur pour les tokens liés à un utilisateur existant.
     * Le constructeur s'assure que si l'utilisateur est fourni, il ne peut pas être null.
     */
    public VerificationToken(String token, User user, TokenType tokenType, Instant expiryDate, String payload) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être nul lors de l'utilisation de ce constructeur.");
        }
        this.token = token;
        this.user = user;
        this.tokenType = tokenType;
        this.expiryDate = expiryDate;
        this.payload = payload;
    }

    public VerificationToken(String token, TokenType tokenType, Instant expiryDate, String payload) {
        this.token = token;
        this.user = null; // L'utilisateur est explicitement null
        this.tokenType = tokenType;
        this.expiryDate = expiryDate;
        this.payload = payload;
    }
}