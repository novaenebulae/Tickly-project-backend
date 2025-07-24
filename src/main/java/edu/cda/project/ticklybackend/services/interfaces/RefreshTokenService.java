package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.models.token.RefreshToken;
import edu.cda.project.ticklybackend.models.user.User;

import java.util.Optional;

public interface RefreshTokenService {

    /**
     * Creates a new refresh token for a user.
     * Generates a secure random token, hashes it, creates a RefreshToken entity,
     * saves it to the database, and returns the raw (unhashed) token string.
     *
     * @param user the user for whom to create the token
     * @return the raw (unhashed) refresh token string
     */
    String createRefreshToken(User user);

    /**
     * Verifies that a token is not expired.
     * Throws an exception if the token is expired.
     *
     * @param token the token to verify
     * @return the verified token
     * @throws RuntimeException if the token is expired
     */
    RefreshToken verifyExpiration(RefreshToken token);

    /**
     * Finds a token by its hashed value.
     *
     * @param token the raw token to find (will be hashed before lookup)
     * @return an Optional containing the token if found, empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Revokes a token by setting its revoked flag to true.
     *
     * @param token the token to revoke
     */
    void revokeToken(RefreshToken token);

    /**
     * Revokes all tokens for a user.
     *
     * @param user the user whose tokens should be revoked
     */
    void revokeAllUserTokens(User user);
    
    /**
     * Deletes all expired and revoked tokens from the database.
     * This method is intended to be called periodically by a scheduled task.
     */
    void deleteExpiredTokens();
}