package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;

import java.time.Duration;
import java.util.Map;

/**
 * Service pour la gestion des tokens de vérification à usage unique.
 */
public interface TokenService {

    /**
     * Crée un nouveau token de vérification pour un utilisateur.
     *
     * @param user      L'utilisateur associé au token.
     * @param tokenType Le type de token à créer.
     * @param validity  La durée de validité du token.
     * @param payload   Données contextuelles optionnelles (JSON).
     * @return Le token de vérification créé et sauvegardé.
     */
    VerificationToken createToken(User user, TokenType tokenType, Duration validity, String payload);

    /**
     * Valide un token.
     *
     * @param tokenString  Le token à valider.
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

    /**
     * Analyse la chaîne de caractères JSON du payload d'un token et la retourne sous forme de Map.
     *
     * @param token Le token dont le payload doit être analysé.
     * @return Une Map représentant le contenu du payload.
     * @throws InvalidTokenException si le payload ne peut pas être analysé.
     */
    Map<String, Object> getPayload(VerificationToken token) throws InvalidTokenException;
}