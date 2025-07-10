package edu.cda.project.ticklybackend.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.mailing.VerificationTokenRepository;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final VerificationTokenRepository tokenRepository;
    private final ObjectMapper objectMapper; // Injection de Jackson ObjectMapper

    @Override
    @Transactional
    public VerificationToken createToken(User user, TokenType tokenType, Duration validity, String payload) {
        LoggingUtils.logMethodEntry(log, "createToken", "user", user != null ? user.getEmail() : "null", 
                "tokenType", tokenType, "validity", validity, "payload", payload);

        try {
            log.debug("Début de la création d'un token de type {} avec une validité de {}", tokenType, validity);
            String tokenString = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().plus(validity);

            if (user != null) {
                LoggingUtils.setUserId(user.getId());
            }

            VerificationToken verificationToken;
            if (user != null) {
                log.debug("Création d'un token pour l'utilisateur ID: {}, email: {}", user.getId(), user.getEmail());
                verificationToken = new VerificationToken(tokenString, user, tokenType, expiryDate, payload);
            } else {
                // Uniquement pour les invitations, on utilise le constructeur qui autorise un utilisateur null.
                log.debug("Création d'un token sans utilisateur associé (probablement pour une invitation)");
                verificationToken = new VerificationToken(tokenString, tokenType, expiryDate, payload);
            }

            VerificationToken savedToken = tokenRepository.save(verificationToken);
            log.info("Token de type {} créé avec succès, ID: {}, expiration: {}", tokenType, savedToken.getId(), expiryDate);

            LoggingUtils.logMethodExit(log, "createToken", savedToken);
            return savedToken;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la création d'un token de type " + tokenType, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationToken validateToken(String tokenString, TokenType expectedType) throws InvalidTokenException {
        LoggingUtils.logMethodEntry(log, "validateToken", "tokenString", tokenString, "expectedType", expectedType);

        try {
            log.debug("Tentative de validation du token: {} (type attendu: {})", tokenString, expectedType);

            VerificationToken token = tokenRepository.findByToken(tokenString)
                    .orElseThrow(() -> {
                        log.warn("Token non trouvé: {}", tokenString);
                        return new InvalidTokenException("Token non trouvé.");
                    });

            log.debug("Token trouvé, ID: {}, type: {}, utilisateur: {}", 
                    token.getId(), token.getTokenType(), token.getUser() != null ? token.getUser().getEmail() : "aucun");

            if (token.getUser() != null) {
                LoggingUtils.setUserId(token.getUser().getId());
            }

            if (token.isUsed()) {
                log.warn("Tentative d'utilisation d'un token déjà utilisé, ID: {}", token.getId());
                throw new InvalidTokenException("Ce token a déjà été utilisé.");
            }

            if (token.getExpiryDate().isBefore(Instant.now())) {
                log.warn("Tentative d'utilisation d'un token expiré, ID: {}, date d'expiration: {}", 
                        token.getId(), token.getExpiryDate());
                throw new InvalidTokenException("Ce token a expiré.");
            }

            if (token.getTokenType() != expectedType) {
                log.warn("Type de token incorrect, ID: {}, type attendu: {}, type réel: {}", 
                        token.getId(), expectedType, token.getTokenType());
                throw new InvalidTokenException("Le type de token est incorrect.");
            }

            log.info("Token validé avec succès, ID: {}, type: {}", token.getId(), token.getTokenType());

            LoggingUtils.logMethodExit(log, "validateToken", token);
            return token;
        } catch (InvalidTokenException e) {
            // Pas besoin de logException ici car c'est une exception attendue et déjà loggée
            LoggingUtils.logMethodExit(log, "validateToken", "Token invalide: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur inattendue lors de la validation du token", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional
    public void markTokenAsUsed(VerificationToken token) {
        LoggingUtils.logMethodEntry(log, "markTokenAsUsed", "token.id", token.getId(), "token.type", token.getTokenType());

        try {
            if (token.getUser() != null) {
                LoggingUtils.setUserId(token.getUser().getId());
            }

            log.debug("Marquage du token ID: {} comme utilisé", token.getId());
            token.setUsed(true);
            tokenRepository.save(token);
            log.info("Token ID: {} de type {} marqué comme utilisé avec succès", token.getId(), token.getTokenType());

            LoggingUtils.logMethodExit(log, "markTokenAsUsed");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors du marquage du token ID: " + token.getId() + " comme utilisé", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    /**
     * Analyse la chaîne de caractères JSON du payload d'un token et la retourne sous forme de Map.
     *
     * @param token Le token dont le payload doit être analysé.
     * @return Une Map représentant le contenu du payload.
     * @throws InvalidTokenException si le payload ne peut pas être analysé.
     */
    @Override
    public Map<String, Object> getPayload(VerificationToken token) throws InvalidTokenException {
        log.debug("Tentative d'extraction du payload pour le token ID: {}", token.getId());
        String payloadStr = token.getPayload();

        if (payloadStr == null || payloadStr.isBlank()) {
            log.debug("Payload vide ou null pour le token ID: {}, retour d'une map vide", token.getId());
            return Collections.emptyMap();
        }

        try {
            log.debug("Analyse du payload JSON pour le token ID: {}", token.getId());
            Map<String, Object> payload = objectMapper.readValue(payloadStr, new TypeReference<>() {});
            log.debug("Payload analysé avec succès pour le token ID: {}, {} entrées trouvées", token.getId(), payload.size());
            return payload;
        } catch (JsonProcessingException e) {
            log.error("Échec de l'analyse du payload JSON pour le token ID: {}: {}", token.getId(), e.getMessage(), e);
            throw new InvalidTokenException("Impossible d'analyser le payload du token.");
        }
    }
}
