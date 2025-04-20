package edu.cda.project.ticklybackend.security.jwt;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import edu.cda.project.ticklybackend.security.user.AppUserDetails;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret; // Supposé être une chaîne simple, pas Base64

    @Value("${jwt.expiration.s}") // Utilise la variable de votre fichier
    private long jwtExpirationSeconds; // Renommé pour correspondre

    // Conversion du secret en bytes (une seule fois si possible, mais ici à la volée)
    private byte[] getSecretBytes() {
        return jwtSecret.getBytes(StandardCharsets.UTF_8);
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

        // Utilisation de la méthode signWith dépréciée
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000)) // Calcul avec secondes
                // Utilisation directe des bytes du secret
                .signWith(SignatureAlgorithm.HS256, getSecretBytes()) // C'est cette méthode qui est dépréciée
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
     * Extrait tous les claims en utilisant Jwts.parser() et setSigningKey avec les bytes.
     */
    private Claims extractAllClaims(String token) {
        // Utilisation de Jwts.parser() et setSigningKey avec les bytes du secret
        return Jwts.parser()
                .setSigningKey(getSecretBytes()) // C'est cette méthode qui est dépréciée
                .parseClaimsJws(token)
                .getBody();
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
     * Valide un token JWT (signature et expiration) en utilisant l'ancienne API.
     */
    public boolean isTokenValid(String token) {
        try {
            // Le simple fait d'extraire les claims avec la bonne clé valide la signature et l'expiration
            extractAllClaims(token);
            return true;
            // Les exceptions spécifiques peuvent être différentes ou moins granulaires dans les vieilles versions
        } catch (MalformedJwtException e) { // Peut exister dans les vieilles versions
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) { // Devrait exister
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { // Peut exister
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) { // Souvent pour token vide/null
            logger.error("JWT claims string is empty or invalid: {}", e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) { // Exception de signature
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (Exception e) { // Catch générique pour d'autres erreurs potentielles
            logger.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Si l'extraction lève déjà cette exception, il est expiré
        } catch (Exception e) {
            // Si une autre erreur survient, on ne peut pas le valider, considérons-le expiré/invalide
            logger.warn("Could not determine expiration due to error: {}", e.getMessage());
            return true;
        }
    }

}
