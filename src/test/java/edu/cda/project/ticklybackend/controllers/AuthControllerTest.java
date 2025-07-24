package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.auth.*;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @Mock
    private User user;

    @InjectMocks
    private AuthController authController;

    private AuthResponseDto mockAuthResponse;
    private UserRegistrationDto registrationDto;
    private UserLoginDto loginDto;
    private PasswordResetRequestDto passwordResetRequestDto;
    private PasswordResetDto passwordResetDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock auth response
        mockAuthResponse = new AuthResponseDto();
        mockAuthResponse.setAccessToken("test-token");
        mockAuthResponse.setTokenType("Bearer");
        mockAuthResponse.setExpiresIn(3600000L);
        mockAuthResponse.setEmail("test@example.com");

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

        // Setup password reset request DTO
        passwordResetRequestDto = new PasswordResetRequestDto();
        passwordResetRequestDto.setEmail("test@example.com");

        // Setup password reset DTO
        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken("reset-token");
        passwordResetDto.setNewPassword("NewPassword123!");
    }

    @Test
    void registerAndLoginUser_ShouldReturnAuthResponseDto() {
        // Arrange
        when(authService.registerUser(registrationDto)).thenReturn(mockAuthResponse);

        // Act
        ResponseEntity<AuthResponseDto> response = authController.registerAndLoginUser(registrationDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(3600000L, response.getBody().getExpiresIn());
        assertEquals("test@example.com", response.getBody().getEmail());

        // Verify
        verify(authService, times(1)).registerUser(registrationDto);
    }

    @Test
    void loginUser_ShouldReturnAuthResponseDto() {
        // Arrange
        when(authService.login(loginDto)).thenReturn(mockAuthResponse);

        // Act
        ResponseEntity<AuthResponseDto> response = authController.loginUser(loginDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(3600000L, response.getBody().getExpiresIn());
        assertEquals("test@example.com", response.getBody().getEmail());

        // Verify
        verify(authService, times(1)).login(loginDto);
    }

    @Test
    void validateEmail_ShouldReturnAuthResponseDto() {
        // Arrange
        String token = "validation-token";
        when(authService.validateEmail(token)).thenReturn(mockAuthResponse);

        // Act
        ResponseEntity<AuthResponseDto> response = authController.validateEmail(token);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(3600000L, response.getBody().getExpiresIn());
        assertEquals("test@example.com", response.getBody().getEmail());

        // Verify
        verify(authService, times(1)).validateEmail(token);
    }

    @Test
    void forgotPassword_ShouldReturnOk() {
        // Arrange
        doNothing().when(authService).forgotPassword(passwordResetRequestDto.getEmail());

        // Act
        ResponseEntity<Void> response = authController.forgotPassword(passwordResetRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(authService, times(1)).forgotPassword(passwordResetRequestDto.getEmail());
    }

    @Test
    void resetPassword_ShouldReturnOk() {
        // Arrange
        doNothing().when(authService).resetPassword(passwordResetDto);

        // Act
        ResponseEntity<Void> response = authController.resetPassword(passwordResetDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(authService, times(1)).resetPassword(passwordResetDto);
    }

    @Test
    void refreshToken_ShouldReturnAuthResponseDto() {
        // Arrange
        RefreshTokenRequestDto refreshTokenRequest = new RefreshTokenRequestDto();
        refreshTokenRequest.setRefreshToken("test-refresh-token");
        
        AuthResponseDto expectedResponse = new AuthResponseDto();
        expectedResponse.setAccessToken("new-token");
        expectedResponse.setTokenType("Bearer");
        expectedResponse.setExpiresIn(3600000L);
        expectedResponse.setRefreshToken("new-refresh-token");

        when(authService.refreshToken(refreshTokenRequest.getRefreshToken())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponseDto> response = authController.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(3600000L, response.getBody().getExpiresIn());
        assertEquals("new-refresh-token", response.getBody().getRefreshToken());

        // Verify
        verify(authService, times(1)).refreshToken(refreshTokenRequest.getRefreshToken());
    }
    
    @Test
    void logout_ShouldReturnOk() {
        // Arrange
        RefreshTokenRequestDto refreshTokenRequest = new RefreshTokenRequestDto();
        refreshTokenRequest.setRefreshToken("test-refresh-token");
        
        doNothing().when(authService).logout(refreshTokenRequest.getRefreshToken());
        
        // Act
        ResponseEntity<Void> response = authController.logout(refreshTokenRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify
        verify(authService, times(1)).logout(refreshTokenRequest.getRefreshToken());
    }
}
