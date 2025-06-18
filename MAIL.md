// Path: src/main/java/edu/cda/project/ticklybackend/enums/TokenType.java
package edu.cda.project.ticklybackend.enums;

/**

* Énumération des différents types de tokens de vérification utilisés dans l'application.
* Chaque type correspond à une action utilisateur spécifique.
  */
  public enum TokenType {
  /**
    * Token envoyé pour valider l'adresse e-mail d'un nouvel utilisateur.
      */
      EMAIL_VALIDATION,

  /**
    * Token envoyé pour permettre la réinitialisation d'un mot de passe oublié.
      */
      PASSWORD_RESET,

  /**
    * Token envoyé pour inviter un utilisateur à rejoindre une équipe.
      */
      TEAM_INVITATION,

  /**
    * Token envoyé pour confirmer la suppression d'un compte.
      */
      ACCOUNT_DELETION_CONFIRMATION
      }

```java
// Path: src/main/java/edu/cda/project/ticklybackend/models/token/VerificationToken.java
package edu.cda.project.ticklybackend.models.token;

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
```java
// Path: src/main/java/edu/cda/project/ticklybackend/repositories/VerificationTokenRepository.java
package edu.cda.project.ticklybackend.repositories;

import edu.cda.project.ticklybackend.models.token.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Trouve un token par sa chaîne de caractères unique.
     * @param token Le token à rechercher.
     * @return un Optional contenant le VerificationToken s'il est trouvé.
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Supprime tous les tokens dont la date d'expiration est antérieure à l'instant fourni.
     * @param now L'instant de comparaison.
     */
    void deleteByExpiryDateBefore(Instant now);
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/exceptions/InvalidTokenException.java
package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsqu'un token de vérification est invalide, expiré, déjà utilisé ou non trouvé.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/config/AsyncConfig.java
package edu.cda.project.ticklybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration pour activer le traitement asynchrone et les tâches planifiées.
 * @EnableAsync permet l'exécution de méthodes @Async dans des threads séparés.
 * @EnableScheduling active la détection et l'exécution de méthodes @Scheduled.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/scheduling/TokenCleanupTask.java
package edu.cda.project.ticklybackend.scheduling;

import edu.cda.project.ticklybackend.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Tâche planifiée pour nettoyer périodiquement les tokens de vérification expirés de la base de données.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {

    private final VerificationTokenRepository tokenRepository;

    /**
     * S'exécute tous les jours à 3h du matin pour supprimer les tokens expirés.
     * La cron expression "0 0 3 * * ?" signifie : à la seconde 0, minute 0, heure 3, tous les jours.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Début de la tâche de nettoyage des tokens expirés.");
        try {
            tokenRepository.deleteByExpiryDateBefore(Instant.now());
            log.info("Nettoyage des tokens expirés terminé avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la tâche de nettoyage des tokens expirés.", e);
        }
    }
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/services/interfaces/TokenService.java
package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.token.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;

import java.time.Duration;

/**
 * Service pour la gestion des tokens de vérification à usage unique.
 */
public interface TokenService {

    /**
     * Crée un nouveau token de vérification pour un utilisateur.
     *
     * @param user       L'utilisateur associé au token.
     * @param tokenType  Le type de token à créer.
     * @param validity   La durée de validité du token.
     * @param payload    Données contextuelles optionnelles (JSON).
     * @return Le token de vérification créé et sauvegardé.
     */
    VerificationToken createToken(User user, TokenType tokenType, Duration validity, String payload);

    /**
     * Valide un token.
     *
     * @param tokenString Le token à valider.
     * @param expectedType Le type de token attendu.
     * @return Le VerificationToken s'il est valide.
     * @throws InvalidTokenException si le token est invalide, expiré, déjà utilisé ou d'un type incorrect.
     */
    VerificationToken validateToken(String tokenString, TokenType expectedType) throws InvalidTokenException;

    /**
     * Marque un token comme utilisé.
     *
     * @param token Le token à marquer.
     */
    void markTokenAsUsed(VerificationToken token);
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/services/impl/TokenServiceImpl.java
package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.token.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.VerificationTokenRepository;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final VerificationTokenRepository tokenRepository;

    @Override
    @Transactional
    public VerificationToken createToken(User user, TokenType tokenType, Duration validity, String payload) {
        String tokenString = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(validity);
        VerificationToken verificationToken = new VerificationToken(tokenString, user, tokenType, expiryDate, payload);
        return tokenRepository.save(verificationToken);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationToken validateToken(String tokenString, TokenType expectedType) throws InvalidTokenException {
        VerificationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new InvalidTokenException("Le token fourni est invalide."));

        if (token.isUsed()) {
            throw new InvalidTokenException("Ce token a déjà été utilisé.");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Ce token a expiré.");
        }

        if (token.getTokenType() != expectedType) {
            throw new InvalidTokenException("Le type de token est incorrect.");
        }

        return token;
    }

    @Override
    @Transactional
    public void markTokenAsUsed(VerificationToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/models/user/User.java
package edu.cda.project.ticklybackend.models.user;

// ... autres imports
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", /* ... */)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements UserDetails {

    // ... champs existants

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isEmailValidated = false;

    // ... méthodes existantes
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/services/interfaces/AuthService.java
package edu.cda.project.ticklybackend.services.interfaces;

// ... autres imports
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;
import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetDto;
import edu.cda.project.ticklybackend.exceptions.EmailAlreadyExistsException;

public interface AuthService {
    
    // ... méthode login existante
    AuthResponseDto login(String email, String password);

    /**
     * Inscrit un nouvel utilisateur mais ne le connecte pas.
     * Envoie un e-mail de validation.
     * @param registrationDto DTO contenant les informations d'inscription.
     * @throws EmailAlreadyExistsException si l'e-mail est déjà utilisé.
     */
    void registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException;

    /**
     * Valide l'e-mail d'un utilisateur via un token et retourne un token JWT.
     * @param token Le token de validation reçu par e-mail.
     * @return Un DTO de réponse d'authentification avec un token JWT.
     */
    AuthResponseDto validateEmail(String token);

    /**
     * Déclenche le processus de mot de passe oublié pour un utilisateur.
     * @param email L'adresse e-mail de l'utilisateur.
     */
    void forgotPassword(String email);

    /**
     * Réinitialise le mot de passe de l'utilisateur à l'aide d'un token.
     * @param passwordResetDto DTO contenant le token et le nouveau mot de passe.
     */
    void resetPassword(PasswordResetDto passwordResetDto);
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/services/impl/AuthServiceImpl.java
package edu.cda.project.ticklybackend.services.impl;

// ... autres imports
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.token.VerificationToken;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Duration;

@Service
public class AuthServiceImpl implements AuthService {

    // ... dépendances existantes
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final MailingService mailingService;
    // ...

    public AuthServiceImpl(/*... autres dépendances,*/ PasswordEncoder passwordEncoder, TokenService tokenService, MailingService mailingService) {
        // ... initialisations
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.mailingService = mailingService;
    }

    // ... méthode login existante

    @Override
    public void registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Un compte existe déjà avec l'adresse e-mail : " + registrationDto.getEmail());
        }

        User newUser;
        if (registrationDto.isCreateStructure()) {
            newUser = new StructureAdministratorUser(registrationDto.getFirstName(), registrationDto.getLastName(), registrationDto.getEmail(), passwordEncoder.encode(registrationDto.getPassword()), null, true);
        } else {
            newUser = new SpectatorUser(registrationDto.getFirstName(), registrationDto.getLastName(), registrationDto.getEmail(), passwordEncoder.encode(registrationDto.getPassword()));
        }
        
        newUser.setEmailValidated(false); // L'email n'est pas encore validé
        User savedUser = userRepository.save(newUser);

        // Créer un token et envoyer l'e-mail de validation
        VerificationToken validationToken = tokenService.createToken(savedUser, TokenType.EMAIL_VALIDATION, Duration.ofHours(24), null);
        String validationLink = "/auth/validate-email?token=" + validationToken.getToken();
        mailingService.sendEmailValidation(savedUser.getEmail(), savedUser.getFirstName(), validationLink);
    }

    @Override
    public AuthResponseDto validateEmail(String tokenString) {
        VerificationToken token = tokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION);
        User user = token.getUser();

        if (user.isEmailValidated()) {
            throw new InvalidTokenException("Cet e-mail a déjà été validé.");
        }

        user.setEmailValidated(true);
        userRepository.save(user);

        tokenService.markTokenAsUsed(token);

        // Connecter l'utilisateur en générant un JWT
        return new AuthResponseDto(jwtTokenProvider.generateToken(user));
    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            VerificationToken resetToken = tokenService.createToken(user, TokenType.PASSWORD_RESET, Duration.ofHours(1), null);
            String resetLink = "/auth/reset-password?token=" + resetToken.getToken();
            mailingService.sendPasswordReset(user.getEmail(), user.getFirstName(), resetLink);
        });
        // Ne pas lever d'exception si l'email n'existe pas pour des raisons de sécurité (ne pas révéler quels emails sont enregistrés).
    }

    @Override
    public void resetPassword(PasswordResetDto passwordResetDto) {
        VerificationToken token = tokenService.validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET);
        User user = token.getUser();
        
        user.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
        userRepository.save(user);

        tokenService.markTokenAsUsed(token);
    }
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/dtos/auth/PasswordResetRequestDto.java
package edu.cda.project.ticklybackend.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDto {
    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    private String email;
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/dtos/auth/PasswordResetDto.java
package edu.cda.project.ticklybackend.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetDto {
    @NotBlank(message = "Le token est requis.")
    private String token;

    @NotBlank(message = "Le nouveau mot de passe ne peut pas être vide.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    private String newPassword;
}
```java
// Path: src/main/java/edu/cda/project/ticklybackend/controllers/AuthController.java
package edu.cda.project.ticklybackend.controllers;

// ... autres imports
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetDto;
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    // ... AuthService injecté

    @Operation(summary = "Inscrire un nouvel utilisateur", description = "Inscrit un nouvel utilisateur (spectateur ou admin de structure) et envoie un e-mail de validation. Ne retourne pas de token JWT à cette étape.")
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        authService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Valider l'e-mail d'un utilisateur", description = "Valide l'e-mail en utilisant le token reçu. En cas de succès, retourne un token JWT pour une connexion automatique.")
    @GetMapping("/validate-email")
    public ResponseEntity<AuthResponseDto> validateEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.validateEmail(token));
    }

    @Operation(summary = "Demander la réinitialisation du mot de passe", description = "Envoie un e-mail avec un lien de réinitialisation si l'adresse e-mail est associée à un compte.")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        authService.forgotPassword(requestDto.getEmail());
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Réinitialiser le mot de passe", description = "Met à jour le mot de passe de l'utilisateur en utilisant le token et le nouveau mot de passe fournis.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        authService.resetPassword(resetDto);
        return ResponseEntity.ok().build();
    }

    // ... endpoint /login existant
}
