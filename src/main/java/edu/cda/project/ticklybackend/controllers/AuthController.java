package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.auth.*;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth") // Chemin de base pour ce contrôleur
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints pour l'inscription et la connexion des utilisateurs")
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
        AuthResponseDto authResponse = authService.registerUser(registrationDto);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Connecter un utilisateur existant",
            description = "Valide les identifiants de l'utilisateur et retourne un token JWT en cas de succès.")
    @ApiResponse(responseCode = "200", description = "Connexion réussie",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Données de connexion invalides")
    @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(
            @Valid @RequestBody UserLoginDto loginDto) {
        AuthResponseDto authResponse = authService.login(loginDto);
        return ResponseEntity.ok(authResponse);
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
}