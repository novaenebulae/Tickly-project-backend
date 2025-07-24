package edu.cda.project.ticklybackend.scheduling;

import edu.cda.project.ticklybackend.repositories.mailing.VerificationTokenRepository;
import edu.cda.project.ticklybackend.services.interfaces.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduled tasks to clean up expired and revoked refresh tokens from the database.
 * This helps keep the database size manageable and improves performance.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {

    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository tokenRepository;

    /**
     * Deletes all expired and revoked refresh tokens from the database.
     * Runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredTokens() {

        log.info("Starting scheduled cleanup of expired and revoked tokens");
        try {
            refreshTokenService.deleteExpiredTokens();
            tokenRepository.deleteByExpiryDateBefore(Instant.now());
            log.info("Completed scheduled cleanup of expired and revoked tokens");
        } catch (Exception e) {
            log.error("Token cleanup error : ", e);
        }
    }

}