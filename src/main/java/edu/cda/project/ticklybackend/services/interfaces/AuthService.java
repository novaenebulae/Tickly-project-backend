package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetDto;
import edu.cda.project.ticklybackend.dtos.auth.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;
import edu.cda.project.ticklybackend.exceptions.EmailAlreadyExistsException;


public interface AuthService {

    AuthResponseDto login(UserLoginDto loginDto);

    /**
     * Inscrit un nouvel utilisateur mais ne le connecte pas.
     * Envoie un e-mail de validation.
     *
     * @param registrationDto DTO contenant les informations d'inscription.
     * @return
     * @throws EmailAlreadyExistsException si l'e-mail est déjà utilisé.
     */
    AuthResponseDto registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException;

    /**
     * Valide l'e-mail d'un utilisateur via un token et retourne un token JWT.
     *
     * @param token Le token de validation reçu par e-mail.
     * @return Un DTO de réponse d'authentification avec un token JWT.
     */
    AuthResponseDto validateEmail(String token);

    /**
     * Déclenche le processus de mot de passe oublié pour un utilisateur.
     *
     * @param email L'adresse e-mail de l'utilisateur.
     */
    void forgotPassword(String email);

    /**
     * Réinitialise le mot de passe de l'utilisateur à l'aide d'un token.
     *
     * @param passwordResetDto DTO contenant le token et le nouveau mot de passe.
     */
    void resetPassword(PasswordResetDto passwordResetDto);
}
