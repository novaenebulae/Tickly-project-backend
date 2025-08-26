package edu.cda.project.ticklybackend.config;

import edu.cda.project.ticklybackend.security.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-level configuration for authentication components.
 * <p>
 * Declares beans for PasswordEncoder, AuthenticationProvider, and AuthenticationManager
 * used by Spring Security across the application.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AppUserDetailsService userDetailsService;

    /**
     * Provides a BCrypt-based password encoder for hashing user credentials.
     *
     * @return a PasswordEncoder using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication provider using the application-specific UserDetailsService
     * and the provided PasswordEncoder.
     *
     * @param passwordEncoder the encoder used to verify passwords
     * @return a fully configured DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager from Spring's AuthenticationConfiguration.
     *
     * @param config Spring Security authentication configuration
     * @return the AuthenticationManager to be used by the application
     * @throws Exception if the manager cannot be obtained
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}