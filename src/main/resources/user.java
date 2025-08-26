import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Représente l'entité 'User' et est mappée à la table 'users' dans la base de données.
 * Chaque instance de cette classe correspond à une ligne dans la table 'users'.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        // Correspond à la contrainte SQL: CONSTRAINT UK_users_email UNIQUE (email)
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le prénom ne peut pas être vide.")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Le nom de famille ne peut pas être vide.")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isEmailValidated = false;

    private String avatarPath;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "consent_given_at", nullable = false)
    private Instant consentGivenAt;

}
