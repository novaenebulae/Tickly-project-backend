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
import edu.cda.project.ticklybackend.models.token.RefreshToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.RefreshTokenService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import edu.cda.project.ticklybackend.services.interfaces.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private MailingService mailingService;

    @Mock
    private TeamManagementService teamService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserRegistrationDto registrationDto;
    private UserLoginDto loginDto;
    private PasswordResetDto passwordResetDto;
    private VerificationToken verificationToken;
    private AuthResponseDto authResponseDto;
    private RefreshToken refreshToken;
    private InvitationAcceptanceResponseDto invitationResponse;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setEmailValidated(true);

        // Setup registration DTO
        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("Password123!");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");

        // Setup login DTO
        loginDto = new UserLoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("Password123!");

        // Setup password reset DTO
        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken("reset-token");
        passwordResetDto.setNewPassword("NewPassword123!");

        // Setup verification token
        verificationToken = new VerificationToken();
        verificationToken.setId(1L);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(testUser);
        verificationToken.setTokenType(TokenType.EMAIL_VALIDATION);
        verificationToken.setExpiryDate(Instant.now().plus(Duration.ofHours(24)));
        verificationToken.setUsed(false);

        // Setup auth response DTO
        authResponseDto = new AuthResponseDto();
        authResponseDto.setAccessToken("test-token");
        authResponseDto.setRefreshToken("refresh-token");
        authResponseDto.setTokenType("Bearer");
        authResponseDto.setExpiresIn(3600000L);
        authResponseDto.setEmail("test@example.com");

        // Setup refresh token
        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("hashed-refresh-token");
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofDays(7)));
        refreshToken.setRevoked(false);

        // Setup invitation response
        invitationResponse = new InvitationAcceptanceResponseDto(
                "invitation-access-token",
                3600000L,
                1L,
                "Test Structure",
                "Invitation acceptée avec succès"
        );
    }

    // Tests pour l'inscription standard
    @Test
    void registerUser_WithoutInvitationToken_ShouldReturnAuthResponseDto() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(verificationTokenService.createToken(any(User.class), eq(TokenType.EMAIL_VALIDATION), any(Duration.class), isNull()))
                .thenReturn(verificationToken);
        when(userMapper.userToAuthResponseDto(any(User.class))).thenReturn(authResponseDto);

        // Act
        AuthResponseDto result = authService.registerUser(registrationDto);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(userRepository).save(any(User.class));
        verify(verificationTokenService).createToken(any(User.class), eq(TokenType.EMAIL_VALIDATION), any(Duration.class), isNull());
        verify(mailingService).sendEmailValidation(eq(testUser.getEmail()), eq(testUser.getFirstName()), anyString());
        verify(userMapper).userToAuthResponseDto(any(User.class));
    }

    @Test
    void registerUser_WithInvitationToken_ShouldReturnAuthResponseDto() {
        // Arrange
        registrationDto.setInvitationToken("invitation-token");
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(teamService.acceptInvitation("invitation-token")).thenReturn(invitationResponse);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(authResponseDto);

        // Act
        AuthResponseDto result = authService.registerUser(registrationDto);

        // Assert
        assertNotNull(result);
        assertEquals("invitation-access-token", result.getAccessToken());
        assertEquals(3600000L, result.getExpiresIn());

        // Verify
        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(userRepository).save(any(User.class));
        verify(teamService).acceptInvitation("invitation-token");
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userMapper).userToAuthResponseDto(testUser);
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowEmailAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.registerUser(registrationDto));

        // Verify
        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verifyNoMoreInteractions(userRepository, passwordEncoder, teamService);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponseDto() {
        // Arrange
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("test-token");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);
        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(authResponseDto);

        // Act
        AuthResponseDto result = authService.login(loginDto);

        // Assert
        assertNotNull(result);
        assertEquals("test-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(userRepository).findByEmail(loginDto.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateAccessToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser);
        verify(userMapper).userToAuthResponseDto(testUser);
    }

    @Test
    void login_WithNonExistentEmail_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginDto));

        // Verify
        verify(userRepository).findByEmail(loginDto.getEmail());
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void login_WithNonValidatedEmail_ShouldThrowBadCredentialsException() {
        // Arrange
        testUser.setEmailValidated(false);
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginDto));

        // Verify
        verify(userRepository).findByEmail(loginDto.getEmail());
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void validateEmail_WithValidToken_ShouldReturnAuthResponseDto() {
        // Arrange
        String tokenString = "valid-token";
        testUser.setEmailValidated(false);
        when(verificationTokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION)).thenReturn(verificationToken);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("test-token");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("refresh-token");
        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(authResponseDto);
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);

        // Act
        AuthResponseDto result = authService.validateEmail(tokenString);

        // Assert
        assertNotNull(result);
        assertEquals("test-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(verificationTokenService).validateToken(tokenString, TokenType.EMAIL_VALIDATION);
        verify(userRepository).save(testUser);
        verify(verificationTokenService).markTokenAsUsed(verificationToken);
        verify(jwtTokenProvider).generateAccessToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser);
        verify(userMapper).userToAuthResponseDto(testUser);
        assertTrue(testUser.isEmailValidated());
    }

    @Test
    void validateEmail_WithAlreadyValidatedEmail_ShouldThrowInvalidTokenException() {
        // Arrange
        String tokenString = "valid-token";
        testUser.setEmailValidated(true);
        when(verificationTokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION)).thenReturn(verificationToken);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authService.validateEmail(tokenString));

        // Verify
        verify(verificationTokenService).validateToken(tokenString, TokenType.EMAIL_VALIDATION);
        verifyNoMoreInteractions(userRepository, verificationTokenService, jwtTokenProvider, userMapper);
    }

    @Test
    void forgotPassword_WithExistingEmail_ShouldSendResetEmail() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(verificationTokenService.createToken(eq(testUser), eq(TokenType.PASSWORD_RESET), any(Duration.class), isNull()))
                .thenReturn(verificationToken);

        // Act
        authService.forgotPassword(email);

        // Verify
        verify(userRepository).findByEmail(email);
        verify(verificationTokenService).createToken(eq(testUser), eq(TokenType.PASSWORD_RESET), any(Duration.class), isNull());
        verify(mailingService).sendPasswordReset(eq(testUser.getEmail()), eq(testUser.getFirstName()), anyString());
    }

    @Test
    void forgotPassword_WithNonExistentEmail_ShouldDoNothing() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        authService.forgotPassword(email);

        // Verify
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(verificationTokenService, mailingService);
    }

    @Test
    void resetPassword_WithValidToken_ShouldUpdatePassword() {
        // Arrange
        when(verificationTokenService.validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET)).thenReturn(verificationToken);
        when(passwordEncoder.encode(passwordResetDto.getNewPassword())).thenReturn("newEncodedPassword");

        // Act
        authService.resetPassword(passwordResetDto);

        // Assert
        assertEquals("newEncodedPassword", testUser.getPassword());

        // Verify
        verify(verificationTokenService).validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET);
        verify(passwordEncoder).encode(passwordResetDto.getNewPassword());
        verify(userRepository).save(testUser);
        verify(verificationTokenService).markTokenAsUsed(verificationToken);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthResponseDto() {
        // Arrange
        String refreshTokenString = "valid-refresh-token";

        // Créer une nouvelle réponse avec les nouveaux tokens pour ce test
        AuthResponseDto newAuthResponse = new AuthResponseDto();
        newAuthResponse.setAccessToken("new-access-token");
        newAuthResponse.setRefreshToken("new-refresh-token");
        newAuthResponse.setTokenType("Bearer");
        newAuthResponse.setExpiresIn(3600000L);
        newAuthResponse.setEmail("test@example.com");

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(newAuthResponse);
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);

        // Act
        AuthResponseDto result = authService.refreshToken(refreshTokenString);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(refreshTokenService).revokeToken(refreshToken);
        verify(jwtTokenProvider).generateAccessToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser);
        verify(userMapper).userToAuthResponseDto(testUser);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowTokenRefreshException() {
        // Arrange
        String refreshTokenString = "invalid-refresh-token";
        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(refreshTokenString));

        // Verify
        verify(refreshTokenService).findByToken(refreshTokenString);
        verifyNoMoreInteractions(refreshTokenService, jwtTokenProvider, userMapper);
    }

    @Test
    void refreshToken_WithRevokedToken_ShouldThrowTokenRefreshException() {
        // Arrange
        String refreshTokenString = "revoked-refresh-token";
        refreshToken.setRevoked(true);
        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        // Act & Assert
        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(refreshTokenString));

        // Verify
        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).revokeAllUserTokens(testUser);
        verifyNoMoreInteractions(refreshTokenService, jwtTokenProvider, userMapper);
    }

    @Test
    void logout_WithValidToken_ShouldRevokeToken() {
        // Arrange
        String refreshTokenString = "valid-refresh-token";
        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        // Act
        authService.logout(refreshTokenString);

        // Verify
        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).revokeToken(refreshToken);
    }

    @Test
    void logout_WithInvalidToken_ShouldDoNothing() {
        // Arrange
        String refreshTokenString = "invalid-refresh-token";
        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        // Act
        authService.logout(refreshTokenString);

        // Verify
        verify(refreshTokenService).findByToken(refreshTokenString);
        verifyNoMoreInteractions(refreshTokenService);
    }
}