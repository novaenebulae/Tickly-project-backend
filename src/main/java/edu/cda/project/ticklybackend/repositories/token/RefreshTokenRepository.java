package edu.cda.project.ticklybackend.repositories.token;

import edu.cda.project.ticklybackend.models.token.RefreshToken;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find a refresh token by its token value (hashed)
     * 
     * @param token the hashed token value
     * @return an Optional containing the refresh token if found, empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Delete all refresh tokens for a specific user
     * 
     * @param user the user whose tokens should be deleted
     */
    void deleteAllByUser(User user);
    
    /**
     * Find all refresh tokens for a specific user
     * 
     * @param user the user whose tokens should be retrieved
     * @return a list of refresh tokens
     */
    List<RefreshToken> findAllByUser(User user);
    
    /**
     * Find all expired or revoked tokens
     * 
     * @param now the current time
     * @return a list of expired or revoked tokens
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.expiryDate < ?1 OR r.revoked = true")
    List<RefreshToken> findAllExpiredOrRevoked(Instant now);
    
    /**
     * Delete all expired or revoked tokens
     * 
     * @param now the current time
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < ?1 OR r.revoked = true")
    void deleteAllExpiredOrRevoked(Instant now);
}