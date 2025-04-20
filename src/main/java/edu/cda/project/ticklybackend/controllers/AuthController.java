package edu.cda.project.ticklybackend.controllers;

// Imports nécessaires

import edu.cda.project.ticklybackend.dtos.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.UserRegistrationDto;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.security.jwt.JwtUtils;
import edu.cda.project.ticklybackend.security.user.AppUserDetails;
import edu.cda.project.ticklybackend.services.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin // Garder si nécessaire pour le dev frontend
@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Dépendances principales pour ce contrôleur
    private final AuthService authService;
    private final AuthenticationProvider authenticationProvider;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthService authService, // Injection du nouveau service
                          AuthenticationProvider authenticationProvider,
                          JwtUtils jwtUtils) {
        this.authService = authService;
        this.authenticationProvider = authenticationProvider;
        this.jwtUtils = jwtUtils;
    } // << MANQUAIT CETTE ACCOLADE

    /**
     * Endpoint pour l'inscription d'un nouvel utilisateur.
     * Délègue la logique à AuthService et retourne un JWT et des informations utilisateur.
     *
     * @param userDto Données d'inscription validées.
     * @return ResponseEntity contenant AuthResponseDto en cas de succès, ou un message d'erreur sinon.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto userDto) {
        logger.info("Received registration request for email: {}", userDto.getEmail());
        try {
            AuthResponseDto response = authService.registerAndLogin(userDto);
            // Succès : retourner 200 OK (ou 201 Created) avec le DTO
            return ResponseEntity.ok(response);
        } catch (edu.cda.project.ticklybackend.exception.EmailAlreadyExistsException e) {
            logger.warn("Registration conflict for email {}: {}", userDto.getEmail(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body(e.getMessage()); // Renvoyer le message de l'exception
        } catch (Exception e) {
            // Gérer toute autre erreur interne potentielle
            logger.error("Internal error during registration for email {}: {}", userDto.getEmail(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur interne est survenue lors de l'inscription.");
        }
    } // << MANQUAIT CETTE ACCOLADE

    /**
     * Endpoint pour la connexion d'un utilisateur existant.
     * Valide les identifiants et retourne un JWT et des informations utilisateur.
     *
     * @param loginDto Données de connexion validées.
     * @return ResponseEntity contenant AuthResponseDto en cas de succès, ou une erreur 401 sinon.
     */
    @PostMapping("/login")
    // Retourne maintenant ResponseEntity<AuthResponseDto>
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto loginDto) {
        logger.info("Received login request for email: {}", loginDto.getEmail());
        try {
            // Authentifier l'utilisateur via le provider
            AppUserDetails userDetails = (AppUserDetails) authenticationProvider.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDto.getEmail(),
                                    loginDto.getPassword()))
                    .getPrincipal();

            logger.debug("Authentication successful for email: {}", loginDto.getEmail());

            // Générer le token (JwtUtils gère l'inclusion conditionnelle de needsStructureSetup)
            String token = jwtUtils.generateJwtToken(userDetails);

            // Récupérer l'utilisateur pour déterminer l'état actuel
            User user = userDetails.getUser();
            boolean needsSetup = jwtUtils.extractClaim(token, claims -> claims.get("needsStructureSetup", Boolean.class)) != null;
            // Alternative : recalculer comme dans JwtUtils
            // boolean needsSetup = (user.getRole() == UserRole.STRUCTURE_ADMINISTRATOR && user instanceof StaffUser && ((StaffUser) user).getStructure() == null);


            // Créer le DTO de réponse
            AuthResponseDto responseDto = new AuthResponseDto(
                    token,
                    user.getId(),
                    user.getRole().name(),
                    needsSetup
            );

            return ResponseEntity.ok(responseDto); // Retourner 200 OK avec le DTO

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for email {}: {}", loginDto.getEmail(), e.getMessage());
            // Utiliser un corps de réponse plus explicite pour l'erreur 401
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect.");
        } catch (Exception e) {
            logger.error("Internal error during login for email {}: {}", loginDto.getEmail(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur interne est survenue lors de la connexion.");
        }
    } // << MANQUAIT CETTE ACCOLADE
} // <<<=== Accolade fermante de la classe MANQUANTE
