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
        String tokenString = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(validity);

        VerificationToken verificationToken;
        if (user != null) {
            verificationToken = new VerificationToken(tokenString, user, tokenType, expiryDate, payload);
        } else {
            // Uniquement pour les invitations, on utilise le constructeur qui autorise un utilisateur null.
            verificationToken = new VerificationToken(tokenString, tokenType, expiryDate, payload);
        }

        return tokenRepository.save(verificationToken);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationToken validateToken(String tokenString, TokenType expectedType) throws InvalidTokenException {
        VerificationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new InvalidTokenException("Token non trouvé."));

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

    /**
     * Implémentation de la méthode pour parser le payload.
     */
    @Override
    public Map<String, Object> getPayload(VerificationToken token) throws InvalidTokenException {
        String payloadStr = token.getPayload();
        if (payloadStr == null || payloadStr.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(payloadStr, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Échec de l'analyse du payload JSON pour le token ID {}", token.getId(), e);
            throw new InvalidTokenException("Impossible d'analyser le payload du token.");
        }
    }
}