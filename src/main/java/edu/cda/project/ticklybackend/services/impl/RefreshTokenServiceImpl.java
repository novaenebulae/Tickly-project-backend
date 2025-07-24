package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.exceptions.TokenRefreshException;
import edu.cda.project.ticklybackend.models.token.RefreshToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.token.RefreshTokenRepository;
import edu.cda.project.ticklybackend.services.interfaces.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration.refresh-token-ms}")
    private long refreshTokenDurationMs;

    /**
     * Creates a new refresh token for a user.
     * Generates a secure random token, hashes it, creates a RefreshToken entity,
     * saves it to the database, and returns the raw (unhashed) token string.
     *
     * @param user the user for whom to create the token
     * @return the raw (unhashed) refresh token string
     */
    @Override
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateSecureToken();
        String hashedToken = passwordEncoder.encode(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created new refresh token for user: {}", user.getEmail());

        return rawToken;
    }

    /**
     * Verifies that a token is not expired.
     * Throws an exception if the token is expired.
     *
     * @param token the token to verify
     * @return the verified token
     * @throws TokenRefreshException if the token is expired
     */
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new login request");
        }

        return token;
    }

    /**
     * Finds a token by its raw value.
     * The raw token is hashed and then compared with stored hashed tokens.
     *
     * @param rawToken the raw token to find
     * @return an Optional containing the token if found, empty otherwise
     */
    @Override
    public Optional<RefreshToken> findByToken(String rawToken) {
        return refreshTokenRepository.findAll().stream()
                .filter(token -> passwordEncoder.matches(rawToken, token.getToken()))
                .findFirst();
    }

    /**
     * Revokes a token by setting its revoked flag to true.
     *
     * @param token the token to revoke
     */
    @Override
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Revoked refresh token for user: {}", token.getUser().getEmail());
    }

    /**
     * Revokes all tokens for a user.
     *
     * @param user the user whose tokens should be revoked
     */
    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteAllByUser(user);
        log.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Deletes all expired and revoked tokens from the database.
     * This method is intended to be called periodically by a scheduled task.
     */
    @Override
    @Transactional
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        int count = refreshTokenRepository.findAllExpiredOrRevoked(now).size();
        refreshTokenRepository.deleteAllExpiredOrRevoked(now);
        log.info("Deleted {} expired or revoked refresh tokens", count);
    }

    /**
     * Generates a secure random token.
     *
     * @return a secure random token
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[64]; // 512 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}