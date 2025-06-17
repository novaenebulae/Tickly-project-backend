package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// @Service indique que cette classe est un composant Spring de type Service.
@Service
// @RequiredArgsConstructor génère un constructeur pour les champs finaux (pour l'injection de UserRepository).
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Sera injecté

    // Méthode principale de UserDetailsService, appelée par Spring Security lors de l'authentification.
    // Le paramètre 'username' sera l'email de l'utilisateur dans notre cas.
    @Override
    @Transactional(readOnly = true) // Transaction en lecture seule pour optimiser
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche l'utilisateur dans la base de données par son email.
        // L'entité User doit implémenter l'interface UserDetails de Spring Security.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }
}