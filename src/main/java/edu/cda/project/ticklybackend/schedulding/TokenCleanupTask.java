package edu.cda.project.ticklybackend.schedulding;

import edu.cda.project.ticklybackend.repositories.mailing.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Tâche planifiée pour nettoyer périodiquement les tokens de vérification expirés de la base de données.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {

    private final VerificationTokenRepository tokenRepository;

    /**
     * S'exécute tous les jours à 3h du matin pour supprimer les tokens expirés.
     * La cron expression "0 0 3 * * ?" signifie : à la seconde 0, minute 0, heure 3, tous les jours.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Début de la tâche de nettoyage des tokens expirés.");
        try {
            tokenRepository.deleteByExpiryDateBefore(Instant.now());
            log.info("Nettoyage des tokens expirés terminé avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la tâche de nettoyage des tokens expirés.", e);
        }
    }
}