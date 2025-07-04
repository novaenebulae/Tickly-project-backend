package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.models.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}") // Clé secrète pour signer les tokens (depuis application.properties)
    private String jwtSecret;

    @Value("${jwt.expiration.access-token-ms}") // Durée d'expiration du token en ms
    private long jwtExpirationMs;

// Modifier la génération du token pour utiliser user.getStructure() au lieu de ((StaffUser) userDetails).getStructure()

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof User user) {
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());
            // Correction : Vérifier si la structure n'est pas nulle avant d'accéder à son ID
            if (user.getStructure() != null) {
                claims.put("structureId", user.getStructure().getId());
            }
        }

        return createToken(claims, userDetails.getUsername());
    }


    // Crée le token avec les claims et le sujet (email de l'utilisateur)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Sujet du token (généralement l'email/username)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Date d'émission
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Date d'expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signature avec la clé secrète
                .compact();
    }

    // Valide un token JWT
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Extrait l'email du token
        // Vérifie si l'email correspond et si le token n'est pas expiré
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Vérifie si un token est expiré
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extrait la date d'expiration d'un token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrait l'email (nom d'utilisateur) d'un token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrait un claim spécifique d'un token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrait tous les claims d'un token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Récupère la clé de signature à partir du secret JWT
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationInMillis() {
        return jwtExpirationMs;
    }
}