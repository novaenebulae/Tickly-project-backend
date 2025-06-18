package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetDto;
import edu.cda.project.ticklybackend.dtos.auth.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.EmailAlreadyExistsException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.UserMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.StructureAdministratorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final MailingService mailingService;


    @Override
    @Transactional
    public AuthResponseDto registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Un compte existe déjà avec l'adresse e-mail : " + registrationDto.getEmail());
        }

        User newUser;
        if (Boolean.TRUE.equals(registrationDto.getCreateStructure())) {
            newUser = new StructureAdministratorUser(
                    null
            );
        } else {
            newUser = new SpectatorUser(
                    registrationDto.getFirstName(),
                    registrationDto.getLastName(),
                    registrationDto.getEmail(),
                    passwordEncoder.encode(registrationDto.getPassword()),
                    UserRole.SPECTATOR
            );
        }

        newUser.setEmailValidated(false); // L'email n'est pas encore validé
        User savedUser = userRepository.save(newUser);

        // Créer un token et envoyer l'e-mail de validation
        VerificationToken validationToken = tokenService.createToken(savedUser, TokenType.EMAIL_VALIDATION, Duration.ofHours(24), null);
        String validationLink = "/auth/validate-email?token=" + validationToken.getToken();
        mailingService.sendEmailValidation(savedUser.getEmail(), savedUser.getFirstName(), validationLink);

        return userMapper.userToAuthResponseDto(savedUser);
    }


    @Override
    public AuthResponseDto login(UserLoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginDto.getEmail()));

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

        // Connecter l'utilisateur en générant un JWT
        String jwt = jwtTokenProvider.generateToken(user);
        AuthResponseDto authResponse = userMapper.userToAuthResponseDto(user);
        authResponse.setAccessToken(jwt);
        authResponse.setExpiresIn(jwtTokenProvider.getExpirationInMillis());
        return authResponse;
    }

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
}