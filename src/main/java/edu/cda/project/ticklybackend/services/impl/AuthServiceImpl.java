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
import edu.cda.project.ticklybackend.exceptions.TokenRefreshException;
import edu.cda.project.ticklybackend.mappers.user.UserMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.*;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
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
    private final VerificationTokenService verificationTokenService;
    private final MailingService mailingService;
    private final TeamManagementService teamService; // Injection pour le flux optimisé
    private final RefreshTokenService refreshTokenService; // Service pour gérer les refresh tokens

    @Override
    @Transactional
    public AuthResponseDto registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException {
        LoggingUtils.logMethodEntry(log, "registerUser", "registrationDto", registrationDto);

        try {
            if (userRepository.existsByEmail(registrationDto.getEmail())) {
                throw new EmailAlreadyExistsException(registrationDto.getEmail());
            }

            // Si un token d'invitation est fourni, on le traite
            if (StringUtils.hasText(registrationDto.getInvitationToken())) {
                AuthResponseDto result = registerAndAcceptInvitation(registrationDto);
                LoggingUtils.logMethodExit(log, "registerUser", result);
                return result;
            }

            // Sinon, on suit le flux d'inscription standard
            AuthResponseDto result = registerStandardUser(registrationDto);
            LoggingUtils.logMethodExit(log, "registerUser", result);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }


    private AuthResponseDto registerAndAcceptInvitation(UserRegistrationDto registrationDto) {
        LoggingUtils.logMethodEntry(log, "registerAndAcceptInvitation", "registrationDto", registrationDto);

        try {
            log.info("Début du flux d'inscription optimisé pour l'invitation de {}", registrationDto.getEmail());

            // 1. Créer le compte utilisateur basique (sera transformé par le service d'équipe)
            User newUser = new User(
                    registrationDto.getFirstName(),
                    registrationDto.getLastName(),
                    registrationDto.getEmail(),
                    passwordEncoder.encode(registrationDto.getPassword())
            );
            newUser.setEmailValidated(true); // Email validé via invitation

            newUser.setConsentGivenAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

            User savedUser = userRepository.save(newUser);
            LoggingUtils.setUserId(savedUser.getId());

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

            LoggingUtils.logMethodExit(log, "registerAndAcceptInvitation", authResponse);
            return authResponse;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'inscription avec invitation", e);
            throw e;
        }
    }

    /**
     * Gère le flux d'inscription standard (sans invitation).
     */
    private AuthResponseDto registerStandardUser(UserRegistrationDto registrationDto) {
        LoggingUtils.logMethodEntry(log, "registerStandardUser", "registrationDto", registrationDto);

        try {
            log.info("Début du flux d'inscription standard pour {}", registrationDto.getEmail());

            User newUser = new User(
                    registrationDto.getFirstName(),
                    registrationDto.getLastName(),
                    registrationDto.getEmail(),
                    passwordEncoder.encode(registrationDto.getPassword())
            );

            newUser.setEmailValidated(false); // L'email doit être validé

            newUser.setConsentGivenAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

            User savedUser = userRepository.save(newUser);
            LoggingUtils.setUserId(savedUser.getId());

            // Créer un token et envoyer l'e-mail de validation
            VerificationToken validationToken = verificationTokenService.createToken(savedUser, TokenType.EMAIL_VALIDATION, Duration.ofHours(24), null);
            String validationLink = "/auth/validate-email?token=" + validationToken.getToken();
            mailingService.sendEmailValidation(savedUser.getEmail(), savedUser.getFirstName(), validationLink);

            // Retourne une réponse sans token JWT, l'utilisateur doit d'abord valider son e-mail.
            AuthResponseDto result = userMapper.userToAuthResponseDto(savedUser);
            LoggingUtils.logMethodExit(log, "registerStandardUser", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'inscription standard", e);
            throw e;
        }
    }


    @Override
    public AuthResponseDto login(UserLoginDto loginDto) {
        LoggingUtils.logMethodEntry(log, "login", "loginDto", loginDto);

        try {
            log.info("Tentative de connexion pour l'email: {}", loginDto.getEmail());

            User user = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Tentative de connexion avec un email inexistant: {}", loginDto.getEmail());
                        return new ResourceNotFoundException("User", "email", loginDto.getEmail());
                    });

            LoggingUtils.setUserId(user.getId());

            if (!user.isEmailValidated()) {
                log.warn("Tentative de connexion avec un email non validé: {}", loginDto.getEmail());
                throw new BadCredentialsException("Veuillez validez votre e-mail avant de vous connecter.");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginDto.getEmail(),
                                loginDto.getPassword()
                        )
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                AuthResponseDto authResponse = generateAuthResponse(user);

                log.info("Connexion réussie pour l'utilisateur: {}", user.getEmail());
                LoggingUtils.logMethodExit(log, "login", authResponse);
                return authResponse;
            } catch (BadCredentialsException e) {
                log.warn("Échec de connexion pour l'email: {} (mot de passe incorrect)", loginDto.getEmail());
                throw e;
            }
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void forgotPassword(String email) {
        LoggingUtils.logMethodEntry(log, "forgotPassword", "email", email);

        try {
            log.info("Demande de réinitialisation de mot de passe pour l'email: {}", email);

            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                LoggingUtils.setUserId(user.getId());
                log.info("Utilisateur trouvé pour la réinitialisation de mot de passe: {}", email);
                VerificationToken resetToken = verificationTokenService.createToken(user, TokenType.PASSWORD_RESET, Duration.ofHours(1), null);
                String resetLink = "/reset-password?token=" + resetToken.getToken(); // Lien vers le frontend
                mailingService.sendPasswordReset(user.getEmail(), user.getFirstName(), resetLink);
                log.info("Email de réinitialisation envoyé à: {}", email);
            }, () -> {
                // Ne pas révéler si l'email existe ou non pour des raisons de sécurité
                log.info("Aucun utilisateur trouvé pour l'email: {}, aucun email envoyé", email);
            });

            LoggingUtils.logMethodExit(log, "forgotPassword");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la demande de réinitialisation de mot de passe", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetDto passwordResetDto) {
        LoggingUtils.logMethodEntry(log, "resetPassword", "passwordResetDto", passwordResetDto);

        try {
            log.info("Tentative de réinitialisation de mot de passe avec token");

            VerificationToken token = verificationTokenService.validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET);
            User user = token.getUser();
            LoggingUtils.setUserId(user.getId());
            log.info("Token de réinitialisation valide pour l'utilisateur: {}", user.getEmail());

            user.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
            userRepository.save(user);
            log.info("Mot de passe mis à jour pour l'utilisateur: {}", user.getEmail());

            verificationTokenService.markTokenAsUsed(token);
            log.info("Token de réinitialisation marqué comme utilisé");

            LoggingUtils.logMethodExit(log, "resetPassword");
        } catch (InvalidTokenException e) {
            log.warn("Tentative de réinitialisation avec un token invalide: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la réinitialisation du mot de passe", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    private AuthResponseDto generateAuthResponse(User user) {
        LoggingUtils.logMethodEntry(log, "generateAuthResponse", "user", user);

        try {
            LoggingUtils.setUserId(user.getId());
            log.debug("Génération d'une réponse d'authentification pour l'utilisateur: {}", user.getEmail());

            // Générer un access token
            String jwtToken = jwtTokenProvider.generateAccessToken(user);
            log.debug("Access token JWT généré pour l'utilisateur: {}", user.getEmail());

            // Générer un refresh token
            String refreshToken = refreshTokenService.createRefreshToken(user);
            log.debug("Refresh token généré pour l'utilisateur: {}", user.getEmail());

            AuthResponseDto authResponse = userMapper.userToAuthResponseDto(user);
            authResponse.setAccessToken(jwtToken);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());

            log.debug("Réponse d'authentification complète générée pour l'utilisateur: {}", user.getEmail());
            LoggingUtils.logMethodExit(log, "generateAuthResponse", authResponse);
            return authResponse;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la génération de la réponse d'authentification pour l'utilisateur " + user.getEmail(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponseDto validateEmail(String tokenString) {
        LoggingUtils.logMethodEntry(log, "validateEmail", "tokenString", tokenString);

        try {
            log.info("Tentative de validation d'email avec token");

            VerificationToken token = verificationTokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION);
            User user = token.getUser();
            LoggingUtils.setUserId(user.getId());
            log.info("Token de validation valide pour l'utilisateur: {}", user.getEmail());

            if (user.isEmailValidated()) {
                log.warn("Tentative de validation d'un email déjà validé pour: {}", user.getEmail());
                throw new InvalidTokenException("Cet e-mail a déjà été validé.");
            }

            user.setEmailValidated(true);
            userRepository.save(user);
            log.info("Email validé avec succès pour l'utilisateur: {}", user.getEmail());

            verificationTokenService.markTokenAsUsed(token);
            log.info("Token de validation marqué comme utilisé");

            AuthResponseDto response = generateAuthResponse(user);
            log.info("Token d'authentification généré pour l'utilisateur nouvellement validé: {}", user.getEmail());

            LoggingUtils.logMethodExit(log, "validateEmail", response);
            return response;
        } catch (InvalidTokenException e) {
            log.warn("Tentative de validation avec un token invalide: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la validation de l'email", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(String refreshToken) {
        LoggingUtils.logMethodEntry(log, "refreshToken", "refreshToken", "***");

        try {
            log.info("Demande de rafraîchissement de token avec refresh token");

            // Vérifier si le refresh token existe
            var refreshTokenOpt = refreshTokenService.findByToken(refreshToken);

            if (refreshTokenOpt.isEmpty()) {
                log.warn("Tentative de rafraîchissement avec un token inexistant");
                throw new TokenRefreshException("Refresh token invalide");
            }

            var tokenEntity = refreshTokenOpt.get();

            // Vérifier si le token est révoqué
            if (tokenEntity.isRevoked()) {
                log.warn("Tentative de réutilisation d'un refresh token révoqué pour l'utilisateur: {}", tokenEntity.getUser().getEmail());
                // Révoquer tous les tokens de l'utilisateur en cas de tentative de réutilisation (sécurité)
                refreshTokenService.revokeAllUserTokens(tokenEntity.getUser());
                throw new TokenRefreshException("Tentative de réutilisation d'un refresh token. Tous les tokens ont été révoqués.");
            }

            // Vérifier si le token est expiré
            refreshTokenService.verifyExpiration(tokenEntity);

            User user = tokenEntity.getUser();
            LoggingUtils.setUserId(user.getId());
            log.info("Refresh token valide pour l'utilisateur: {}", user.getEmail());

            // Révoquer le token actuel
            refreshTokenService.revokeToken(tokenEntity);
            log.info("Ancien refresh token révoqué");

            // Générer un nouveau access token
            String accessToken = jwtTokenProvider.generateAccessToken(user);

            // Générer un nouveau refresh token
            String newRefreshToken = refreshTokenService.createRefreshToken(user);
            log.info("Nouveaux tokens générés pour l'utilisateur: {}", user.getEmail());

            // Créer la réponse
            AuthResponseDto response = userMapper.userToAuthResponseDto(user);
            response.setAccessToken(accessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(jwtTokenProvider.getExpirationInMillis());

            LoggingUtils.logMethodExit(log, "refreshToken", "AuthResponseDto");
            return response;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors du rafraîchissement du token", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        LoggingUtils.logMethodEntry(log, "logout", "refreshToken", "***");

        try {
            log.info("Demande de déconnexion avec refresh token");

            // Vérifier si le refresh token existe
            var refreshTokenOpt = refreshTokenService.findByToken(refreshToken);

            if (refreshTokenOpt.isEmpty()) {
                log.warn("Tentative de déconnexion avec un token inexistant");
                return; // Silently ignore invalid tokens
            }

            var tokenEntity = refreshTokenOpt.get();
            User user = tokenEntity.getUser();
            LoggingUtils.setUserId(user.getId());

            // Révoquer le token
            refreshTokenService.revokeToken(tokenEntity);
            log.info("Refresh token révoqué pour l'utilisateur: {}", user.getEmail());

            LoggingUtils.logMethodExit(log, "logout");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la déconnexion", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

}
