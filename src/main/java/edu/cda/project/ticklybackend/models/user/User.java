package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email") // Assure l'unicité de l'email
})
// Stratégie d'héritage : une seule table pour toutes les classes de la hiérarchie User
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Colonne utilisée pour différencier les types d'utilisateurs
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements UserDetails { // Classe abstraite car on ne créera que des sous-classes

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

    // Rôle principal de l'utilisateur, utilisé comme discriminateur
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Structure structure;

    // Chemin vers le fichier avatar de l'utilisateur (sera géré plus tard)
    private String avatarPath;

    @CreationTimestamp // Géré automatiquement par Hibernate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp // Géré automatiquement par Hibernate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "consent_given_at")
    private LocalDateTime consentGivenAt;


    // Implémentation des méthodes de UserDetails pour Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourne une collection d'autorisations basées sur le rôle de l'utilisateur
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // L'email est utilisé comme nom d'utilisateur pour Spring Security
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Logique de compte expiré non implémentée pour le moment
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Logique de compte verrouillé non implémentée pour le moment
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Logique d'identifiants expirés non implémentée pour le moment
    }

    @Override
    public boolean isEnabled() {
        return true; // Logique de compte désactivé non implémentée pour le moment
    }
}