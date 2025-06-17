package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;
import edu.cda.project.ticklybackend.exceptions.EmailAlreadyExistsException;
import edu.cda.project.ticklybackend.mappers.UserMapper;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.StructureAdministratorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public AuthResponseDto registerAndLogin(UserRegistrationDto registrationDto) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException(registrationDto.getEmail());
        }

        User newUser;
        Map<String, Object> extraClaims = new HashMap<>();

        // Créer l'utilisateur en fonction du booléen createStructure
        if (Boolean.TRUE.equals(registrationDto.getCreateStructure())) {
            // Créer un StructureAdministratorUser
            // La structure elle-même sera créée à l'étape 2. Pour l'instant, structure est null.
            newUser = new StructureAdministratorUser(
                    registrationDto.getFirstName(),
                    registrationDto.getLastName(),
                    registrationDto.getEmail(),
                    passwordEncoder.encode(registrationDto.getPassword()),
                    null, // La structure sera liée à l'étape 2
                    true  // needsStructureSetup est true pour un nouvel admin
            );
            extraClaims.put("needsStructureSetup", true);
        } else {
            // Créer un SpectatorUser
            newUser = new SpectatorUser(
                    registrationDto.getFirstName(),
                    registrationDto.getLastName(),
                    registrationDto.getEmail(),
                    passwordEncoder.encode(registrationDto.getPassword())
            );
            // SpectatorUser a needsStructureSetup à false par défaut dans son constructeur
        }

        // Sauvegarder le nouvel utilisateur
        User savedUser = userRepository.save(newUser);

        // Générer un token JWT pour le nouvel utilisateur
        String jwtToken = jwtTokenProvider.generateToken(savedUser, extraClaims);

        // Construire et retourner la réponse d'authentification
        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(savedUser);
        authResponse.setAccessToken(jwtToken);
        authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());
        // Le champ needsStructureSetup est déjà dans les claims du token et dans l'objet User,
        // donc il sera correctement mappé par userMapper si présent dans l'entité User.

        return authResponse;
    }

    @Override
    public AuthResponseDto login(UserLoginDto loginDto) {
        // Authentifier l'utilisateur avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        // Mettre à jour le contexte de sécurité
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Récupérer les détails de l'utilisateur authentifié
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé après authentification")); // Devrait pas arriver

        // Générer un token JWT
        // Récupérer les claims existants pour les inclure dans le nouveau token si nécessaire
        Map<String, Object> extraClaims = new HashMap<>();
        if (user.getNeedsStructureSetup() != null) {
            extraClaims.put("needsStructureSetup", user.getNeedsStructureSetup());
        }

        String jwtToken = jwtTokenProvider.generateToken(user, extraClaims);

        // Construire et retourner la réponse d'authentification
        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(user);
        authResponse.setAccessToken(jwtToken);
        authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());

        return authResponse;
    }
}