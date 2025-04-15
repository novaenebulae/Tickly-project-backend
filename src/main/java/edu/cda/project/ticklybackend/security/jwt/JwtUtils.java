package edu.cda.project.ticklybackend.security.jwt;

import edu.cda.project.ticklybackend.security.user.AppUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class JwtUtils {

    public String generateJwtToken(AppUserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .addClaims(Map.of("role", userDetails.getRole()))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "azerty")
                .compact();
    }

    public String getSubjectFromJwt(String jwt) {

        return Jwts.parser()
                .setSigningKey("azerty")
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }
}
