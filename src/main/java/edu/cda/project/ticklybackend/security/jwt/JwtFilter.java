package edu.cda.project.ticklybackend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String jwt = null;
        String subject = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                subject = jwtUtils.getSubjectFromJwt(jwt);
            } catch (Exception e) {
                logger.warn("Could not get subject from JWT: {}", e.getMessage());
            }
        } else {
            logger.trace("Authorization header does not begin with Bearer String or is missing");
        }

        // Si on a extrait un sujet ET que l'utilisateur n'est pas déjà authentifié ET que le token est valide
        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Charger les détails de l'utilisateur
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(subject);

            // **Ajout de la validation explicite du token**
            if (jwtUtils.isTokenValid(jwt)) { // Vérifier la signature et l'expiration
                // Créer l'objet d'authentification
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()); // Récupérer les rôles/autorités

                // Associer les détails de la requête web
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // **Placer l'authentification dans le contexte de sécurité**
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("User '{}' authenticated successfully.", subject);
            } else {
                logger.warn("JWT token validation failed for user '{}'", subject);
            }
        }

        // Toujours continuer la chaîne de filtres, même si l'authentification a échoué ou n'était pas applicable
        filterChain.doFilter(request, response);

    }
}
