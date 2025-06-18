package edu.cda.project.ticklybackend.repositories.mailing;

import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Trouve un token par sa chaîne de caractères unique.
     *
     * @param token Le token à rechercher.
     * @return un Optional contenant le VerificationToken s'il est trouvé.
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Supprime tous les tokens dont la date d'expiration est antérieure à l'instant fourni.
     *
     * @param now L'instant de comparaison.
     */
    void deleteByExpiryDateBefore(Instant now);

    Iterable<? extends VerificationToken> findByUser(User user);
}