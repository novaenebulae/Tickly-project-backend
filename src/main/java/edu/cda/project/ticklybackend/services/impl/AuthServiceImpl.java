package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetDto;
import edu.cda.project.ticklybackend.dtos.auth.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;
import edu.cda.project.ticklybackend.dtos.team.InvitationAcceptanceResponseDto;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.EmailAlreadyExistsException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.UserMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final MailingService mailingService;
    private final TeamManagementService teamService; // Injection pour le flux optimisé

    @Override
    @Transactional
    public AuthResponseDto registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException(registrationDto.getEmail());
        }

        // Si un token d'invitation est fourni, on le traite
        if (StringUtils.hasText(registrationDto.getInvitationToken())) {
            return registerAndAcceptInvitation(registrationDto);
        }

        // Sinon, on suit le flux d'inscription standard
        return registerStandardUser(registrationDto);
    }


    private AuthResponseDto registerAndAcceptInvitation(UserRegistrationDto registrationDto) {
        log.info("Début du flux d'inscription optimisé pour l'invitation de {}", registrationDto.getEmail());

        // 1. Créer le compte utilisateur basique (sera transformé par le service d'équipe)
        User newUser = new SpectatorUser(
                registrationDto.getFirstName(),
                registrationDto.getLastName(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword())
        );
        newUser.setEmailValidated(true); // Email validé via invitation

        newUser.setConsentGivenAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

        User savedUser = userRepository.save(newUser);

        // 2. Accepter l'invitation (transforme l'utilisateur et retourne les infos complètes)
        InvitationAcceptanceResponseDto invitationResponse = teamService.acceptInvitation(
                registrationDto.getInvitationToken());

        // 3. Convertir la réponse d'invitation en AuthResponseDto
        // Recharger l'utilisateur transformé
        User transformedUser = userRepository.findByEmail(savedUser.getEmail())
                .orElseThrow(() -> new RuntimeException("Erreur lors de la transformation de l'utilisateur"));

        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(transformedUser);
        authResponse.setAccessToken(invitationResponse.getAccessToken());
        authResponse.setExpiresIn(invitationResponse.getExpiresIn());

        log.info("Inscription et invitation complétées pour {} dans la structure {}",
                transformedUser.getEmail(), invitationResponse.getStructureName());

        return authResponse;
    }

    /**
     * Gère le flux d'inscription standard (sans invitation).
     */
    private AuthResponseDto registerStandardUser(UserRegistrationDto registrationDto) {
        log.info("Début du flux d'inscription standard pour {}", registrationDto.getEmail());

        User newUser = new SpectatorUser(
                registrationDto.getFirstName(),
                registrationDto.getLastName(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword())
        );

        newUser.setEmailValidated(false); // L'email doit être validé

        newUser.setConsentGivenAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

        User savedUser = userRepository.save(newUser);

        // Créer un token et envoyer l'e-mail de validation
        VerificationToken validationToken = tokenService.createToken(savedUser, TokenType.EMAIL_VALIDATION, Duration.ofHours(24), null);
        String validationLink = "/auth/validate-email?token=" + validationToken.getToken();
        mailingService.sendEmailValidation(savedUser.getEmail(), savedUser.getFirstName(), validationLink);

        // Retourne une réponse sans token JWT, l'utilisateur doit d'abord valider son e-mail.
        return userMapper.userToAuthResponseDto(savedUser);
    }


    @Override
    public AuthResponseDto login(UserLoginDto loginDto) {

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginDto.getEmail()));

        if (!user.isEmailValidated()) {
            throw new BadCredentialsException("Veuillez validez votre e-mail avant de vous connecter.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);


        String jwtToken = jwtTokenProvider.generateToken(user);

        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(user);
        authResponse.setAccessToken(jwtToken);
        authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());

        return authResponse;
    }

//    @Override
//    @Transactional
//    public AuthResponseDto validateEmail(String tokenString) {
//        VerificationToken token = tokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION);
//        User user = token.getUser();
//
//        if (user.isEmailValidated()) {
//            throw new InvalidTokenException("Cet e-mail a déjà été validé.");
//        }
//
//        user.setEmailValidated(true);
//        userRepository.save(user);
//
//        tokenService.markTokenAsUsed(token);
//
//        // Connecter l'utilisateur en générant un JWT
//        String jwt = jwtTokenProvider.generateToken(user);
//        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(user);
//        authResponse.setAccessToken(jwt);
//        authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());
//        return authResponse;
//    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            VerificationToken resetToken = tokenService.createToken(user, TokenType.PASSWORD_RESET, Duration.ofHours(1), null);
            String resetLink = "/reset-password?token=" + resetToken.getToken(); // Lien vers le frontend
            mailingService.sendPasswordReset(user.getEmail(), user.getFirstName(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetDto passwordResetDto) {
        VerificationToken token = tokenService.validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET);
        User user = token.getUser();

        user.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
        userRepository.save(user);

        tokenService.markTokenAsUsed(token);
    }

    private AuthResponseDto generateAuthResponse(User user) {
        String jwtToken = jwtTokenProvider.generateToken(user);
        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(user);
        authResponse.setAccessToken(jwtToken);
        authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());
        return authResponse;
    }

    @Override
    @Transactional
    public AuthResponseDto validateEmail(String tokenString) {
        VerificationToken token = tokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION);
        User user = token.getUser();
        if (user.isEmailValidated()) {
            throw new InvalidTokenException("Cet e-mail a déjà été validé.");
        }
        user.setEmailValidated(true);
        userRepository.save(user);
        tokenService.markTokenAsUsed(token);
        return generateAuthResponse(user);
    }

    @Override
    public AuthResponseDto refreshToken(User user) {
        // Recharger l'utilisateur depuis la base de données pour s'assurer d'avoir les données les plus récentes
        User refreshedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));

        // Générer un nouveau token JWT
        return generateAuthResponse(refreshedUser);
    }

}
