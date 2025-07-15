package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.AbstractIntegrationTest;
import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.PasswordResetDto;
import edu.cda.project.ticklybackend.dtos.auth.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.UserMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
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
class AuthServiceImplTest extends AbstractIntegrationTest {

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
    private TokenService tokenService;

    @Mock
    private MailingService mailingService;

    @Mock
    private TeamManagementService teamService;

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

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new SpectatorUser();
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
        authResponseDto.setTokenType("Bearer");
        authResponseDto.setExpiresIn(3600000L);
        authResponseDto.setEmail("test@example.com");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponseDto() {
        // Arrange
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("test-token");
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);
        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(authResponseDto);

        // Act
        AuthResponseDto result = authService.login(loginDto);

        // Assert
        assertNotNull(result);
        assertEquals("test-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(userRepository).findByEmail(loginDto.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(testUser);
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
        // Set email as not validated for this test
        testUser.setEmailValidated(false);
        when(tokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION)).thenReturn(verificationToken);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("test-token");
        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(authResponseDto);
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);

        // Act
        AuthResponseDto result = authService.validateEmail(tokenString);

        // Assert
        assertNotNull(result);
        assertEquals("test-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(tokenService).validateToken(tokenString, TokenType.EMAIL_VALIDATION);
        verify(userRepository).save(testUser);
        verify(tokenService).markTokenAsUsed(verificationToken);
        verify(jwtTokenProvider).generateToken(testUser);
        verify(userMapper).userToAuthResponseDto(testUser);
        assertTrue(testUser.isEmailValidated());
    }

    @Test
    void validateEmail_WithAlreadyValidatedEmail_ShouldThrowInvalidTokenException() {
        // Arrange
        String tokenString = "valid-token";
        testUser.setEmailValidated(true);
        when(tokenService.validateToken(tokenString, TokenType.EMAIL_VALIDATION)).thenReturn(verificationToken);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authService.validateEmail(tokenString));

        // Verify
        verify(tokenService).validateToken(tokenString, TokenType.EMAIL_VALIDATION);
        verifyNoMoreInteractions(userRepository, tokenService, jwtTokenProvider, userMapper);
    }

    @Test
    void forgotPassword_WithExistingEmail_ShouldSendResetEmail() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(tokenService.createToken(eq(testUser), eq(TokenType.PASSWORD_RESET), any(Duration.class), isNull()))
                .thenReturn(verificationToken);

        // Act
        authService.forgotPassword(email);

        // Verify
        verify(userRepository).findByEmail(email);
        verify(tokenService).createToken(eq(testUser), eq(TokenType.PASSWORD_RESET), any(Duration.class), isNull());
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
        verifyNoInteractions(tokenService, mailingService);
    }

    @Test
    void resetPassword_WithValidToken_ShouldUpdatePassword() {
        // Arrange
        when(tokenService.validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET)).thenReturn(verificationToken);
        when(passwordEncoder.encode(passwordResetDto.getNewPassword())).thenReturn("newEncodedPassword");

        // Act
        authService.resetPassword(passwordResetDto);

        // Assert
        assertEquals("newEncodedPassword", testUser.getPassword());

        // Verify
        verify(tokenService).validateToken(passwordResetDto.getToken(), TokenType.PASSWORD_RESET);
        verify(passwordEncoder).encode(passwordResetDto.getNewPassword());
        verify(userRepository).save(testUser);
        verify(tokenService).markTokenAsUsed(verificationToken);
    }

    @Test
    void refreshToken_WithValidUser_ShouldReturnNewAuthResponseDto() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("new-token");

        // Create a new auth response with the new token
        AuthResponseDto newAuthResponse = new AuthResponseDto();
        newAuthResponse.setAccessToken("new-token");
        newAuthResponse.setTokenType("Bearer");
        newAuthResponse.setExpiresIn(3600000L);
        newAuthResponse.setEmail("test@example.com");

        when(userMapper.userToAuthResponseDto(testUser)).thenReturn(newAuthResponse);
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);

        // Act
        AuthResponseDto result = authService.refreshToken(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("new-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals("test@example.com", result.getEmail());

        // Verify
        verify(userRepository).findById(testUser.getId());
        verify(jwtTokenProvider).generateToken(testUser);
        verify(userMapper).userToAuthResponseDto(testUser);
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.refreshToken(testUser));

        // Verify
        verify(userRepository).findById(testUser.getId());
        verifyNoInteractions(jwtTokenProvider, userMapper);
    }
}
