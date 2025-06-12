package edu.cda.project.ticklybackend.config;

import edu.cda.project.ticklybackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

// @Configuration indique que cette classe contient des configurations Spring.
@Configuration
// @EnableWebSecurity active la configuration de sécurité web de Spring Security.
@EnableWebSecurity
// @RequiredArgsConstructor génère un constructeur avec les champs finaux requis (pour l'injection).
@RequiredArgsConstructor
// @EnableMethodSecurity active la sécurité au niveau des méthodes (ex : @PreAuthorize).
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider; // Sera injecté (défini plus tard)
    // private final LogoutHandler logoutHandler; // Si vous implémentez un logout côté serveur invalidant les tokens

    // URLs publiques qui ne nécessitent pas d'authentification.
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/static/**",
            "/api/v1/events",
            "/api/v1/events/{id}", // Assurez-vous que les placeholders {id} sont gérés ou que ce sont des chemins exacts
            "/api/v1/events/categories",
            "/api/v1/structures",
            "/api/v1/structures/{id}",
            "/api/v1/structure-types"
    };

    // Bean pour configurer le filtre CORS.
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Autoriser les cookies/authentification
        config.addAllowedOriginPattern("*"); // Autoriser toutes les origines (à restreindre en production)
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // Bean principal pour la chaîne de filtres de sécurité.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF car nous utilisons JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Configurer CORS en utilisant le bean corsFilter()
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(Arrays.asList("*")); // Ou spécifiez vos domaines frontend
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                // Définir les autorisations pour les requêtes HTTP
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll() // Autoriser l'accès public aux URLs définies
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Autoriser les requêtes OPTIONS pour CORS preflight
                        .anyRequest().authenticated() // Toutes les autres requêtes nécessitent une authentification
                )
                // Configurer la gestion de session pour qu'elle soit stateless (pas de session côté serveur)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configurer le fournisseur d'authentification personnalisé
                .authenticationProvider(authenticationProvider)
                // Ajouter notre filtre JWT personnalisé avant le filtre standard UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // Configuration du logout (si nécessaire)
        //.logout(logout -> logout
        //    .logoutUrl("/api/v1/auth/logout")
        //    .addLogoutHandler(logoutHandler) // Votre gestionnaire de logout personnalisé
        //    .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        // );

        return http.build();
    }
}