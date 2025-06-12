package edu.cda.project.ticklybackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// @Component indique que cette classe est un composant Spring géré.
@Component
// @RequiredArgsConstructor génère un constructeur pour les champs finaux.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // Service pour manipuler les JWT (à créer)
    private final UserDetailsService userDetailsService; // Notre AppUserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Récupérer l'en-tête "Authorization" de la requête.
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si l'en-tête est manquant ou ne commence pas par "Bearer ", passer au filtre suivant.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le token JWT (après "Bearer ").
        jwt = authHeader.substring(7);

        try {
            // Extraire l'email de l'utilisateur à partir du token.
            userEmail = jwtTokenProvider.extractUsername(jwt); // ou extractEmail

            // Si l'email est présent et qu'il n'y a pas déjà une authentification dans le contexte de sécurité.
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Charger les détails de l'utilisateur à partir de la base de données.
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Valider le token (vérifier la signature et l'expiration).
                if (jwtTokenProvider.isTokenValid(jwt, userDetails)) {
                    // Créer un objet d'authentification.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Pas de credentials (mot de passe) car authentification par token
                            userDetails.getAuthorities()
                    );
                    // Attacher des détails supplémentaires de la requête à l'authentification.
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Mettre à jour le SecurityContextHolder avec la nouvelle authentification.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur lors de la validation du token (ex: token expiré, malformé),
            // ne pas configurer d'authentification et laisser la requête continuer.
            // Spring Security gérera l'accès non autorisé plus tard.
            // Vous pouvez logger l'exception ici si nécessaire.
            SecurityContextHolder.clearContext(); // Important pour s'assurer qu'un contexte invalide n'est pas utilisé
            // logger.warn("Impossible de définir l'authentification utilisateur : {}", e.getMessage());
        }


        // Passer la requête et la réponse au filtre suivant dans la chaîne.
        filterChain.doFilter(request, response);
    }
}