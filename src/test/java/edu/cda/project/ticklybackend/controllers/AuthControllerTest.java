package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void refreshToken_ShouldReturnAuthResponseDto() {
        // Arrange
        AuthResponseDto expectedResponse = new AuthResponseDto();
        expectedResponse.setAccessToken("new-token");
        expectedResponse.setTokenType("Bearer");
        expectedResponse.setExpiresIn(3600000L);

        when(authentication.getPrincipal()).thenReturn(user);
        when(authService.refreshToken(user)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponseDto> response = authController.refreshToken(authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(3600000L, response.getBody().getExpiresIn());

        // Verify
        verify(authentication, times(1)).getPrincipal();
        verify(authService, times(1)).refreshToken(user);
    }
}