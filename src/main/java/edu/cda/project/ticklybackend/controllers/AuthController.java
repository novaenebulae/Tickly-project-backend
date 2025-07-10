package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.auth.*;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth") // Chemin de base pour ce contrôleur
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints pour l'inscription et la connexion des utilisateurs")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Inscrire un nouvel utilisateur et le connecter",
            description = "Crée un nouveau compte utilisateur et retourne un token JWT si l'inscription réussit.")
    @ApiResponse(responseCode = "201", description = "Utilisateur inscrit et connecté avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Données d'inscription invalides")
    @ApiResponse(responseCode = "409", description = "L'email est déjà utilisé")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerAndLoginUser(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("Demande d'inscription reçue pour l'email: {}", registrationDto.getEmail());
        AuthResponseDto authResponse = authService.registerUser(registrationDto);
        log.info("Inscription réussie pour l'utilisateur: {}", registrationDto.getEmail());
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Connecter un utilisateur existant",
            description = "Valide les identifiants de l'utilisateur et retourne un token JWT en cas de succès.")
    @ApiResponse(responseCode = "200", description = "Connexion réussie",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Données de connexion invalides")
    @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    @ApiResponse(responseCode = "403", description = "Email non validé")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(
            @Valid @RequestBody UserLoginDto loginDto) {
        log.info("Tentative de connexion pour l'email: {}", loginDto.getEmail());
        AuthResponseDto authResponse = authService.login(loginDto);
        log.info("Connexion réussie pour l'utilisateur: {}", loginDto.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Valider l'e-mail d'un utilisateur", description = "Valide l'e-mail en utilisant le token reçu. En cas de succès, retourne un token JWT pour une connexion automatique.")
    @GetMapping("/validate-email")
    public ResponseEntity<AuthResponseDto> validateEmail(@RequestParam("token") String token) {
        log.info("Demande de validation d'email avec token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        AuthResponseDto authResponse = authService.validateEmail(token);
        log.info("Validation d'email réussie pour l'utilisateur: {}", authResponse.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Demander la réinitialisation du mot de passe", description = "Envoie un e-mail avec un lien de réinitialisation si l'adresse e-mail est associée à un compte.")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        log.info("Demande de réinitialisation de mot de passe pour l'email: {}", requestDto.getEmail());
        authService.forgotPassword(requestDto.getEmail());
        log.info("Email de réinitialisation envoyé (si le compte existe) pour: {}", requestDto.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Réinitialiser le mot de passe", description = "Met à jour le mot de passe de l'utilisateur en utilisant le token et le nouveau mot de passe fournis.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        log.info("Demande de réinitialisation de mot de passe avec token: {}", resetDto.getToken().substring(0, Math.min(resetDto.getToken().length(), 10)) + "...");
        authService.resetPassword(resetDto);
        log.info("Réinitialisation de mot de passe réussie");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rafraîchir le token JWT", 
            description = "Génère un nouveau token JWT pour l'utilisateur actuellement authentifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token rafraîchi avec succès", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise")
    })
    @PostMapping("/refresh-token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponseDto> refreshToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Demande de rafraîchissement de token pour l'utilisateur: {}", user.getEmail());
        AuthResponseDto authResponse = authService.refreshToken(user);
        log.info("Token rafraîchi avec succès pour l'utilisateur: {}", user.getEmail());
        return ResponseEntity.ok(authResponse);
    }
}
