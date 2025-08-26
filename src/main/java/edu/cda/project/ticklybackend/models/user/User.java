package edu.cda.project.ticklybackend.models.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email") // Assure l'unicité de l'email
})
public class User implements UserDetails { // Entité concrète d'identité (plus d'héritage, pas de rôle/structure)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Mot de passe haché

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isEmailValidated = false;

    // Chemin vers le fichier avatar de l'utilisateur
    private String avatarPath;

    @CreationTimestamp // Géré automatiquement par Hibernate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp // Géré automatiquement par Hibernate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "consent_given_at")
    private Instant consentGivenAt;

    // Constructeur de commodité pour la création d'un utilisateur identité-seulement
    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    // Implémentation des méthodes de UserDetails pour Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Les autorisations spécifiques aux structures sont gérées via TeamMember; aucun rôle global ici
        return List.of();
    }

    @Override
    public String getUsername() {
        // L'email est utilisé comme nom d'utilisateur pour Spring Security
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}