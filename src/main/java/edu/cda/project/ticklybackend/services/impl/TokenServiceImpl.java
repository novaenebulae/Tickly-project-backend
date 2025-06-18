package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.mailing.VerificationTokenRepository;
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
                .orElseThrow();

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