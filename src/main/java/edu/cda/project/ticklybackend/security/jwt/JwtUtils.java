package edu.cda.project.ticklybackend.security.jwt;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import edu.cda.project.ticklybackend.security.user.AppUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.access-token-ms}")
    private long jwtExpirationMs;

    // Obtenir une clé secrète à partir de la chaîne de secret JWT
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());

        if (shouldIncludeNeedsSetupClaim(user)) {
            claims.put("needsStructureSetup", true);
            logger.debug("JWT Generation: Including needsStructureSetup=true for user ID {}", user.getId());
        } else {
            logger.debug("JWT Generation: Omitting needsStructureSetup claim for user ID {}", user.getId());
        }

        // Utilisation de la nouvelle API JJWT 0.12.3
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateJwtToken(AppUserDetails userDetails) {
        return generateJwtToken(userDetails.getUser());
    }

    private boolean shouldIncludeNeedsSetupClaim(User user) {
        return user.getRole() == UserRole.STRUCTURE_ADMINISTRATOR &&
                user instanceof StaffUser &&
                ((StaffUser) user).getStructure() == null;
    }

    public String getSubjectFromJwt(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait tous les claims en utilisant la nouvelle API JJWT 0.12.3
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.error("Could not extract claim from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Valide un token JWT (signature et expiration) en utilisant la nouvelle API JJWT 0.12.3
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty or invalid: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            logger.warn("Could not determine expiration due to error: {}", e.getMessage());
            return true;
        }
    }

}
