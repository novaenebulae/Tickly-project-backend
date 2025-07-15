package edu.cda.project.ticklybackend.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.repositories.mailing.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User testUser;
    private VerificationToken testToken;
    private String testTokenString;
    private String testPayload;
    private Map<String, Object> testPayloadMap;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new SpectatorUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup test token
        testTokenString = UUID.randomUUID().toString();
        testToken = new VerificationToken();
        testToken.setId(1L);
        testToken.setToken(testTokenString);
        testToken.setUser(testUser);
        testToken.setTokenType(TokenType.EMAIL_VALIDATION);
        testToken.setExpiryDate(Instant.now().plus(Duration.ofHours(24)));
        testToken.setUsed(false);

        // Setup test payload
        testPayloadMap = new HashMap<>();
        testPayloadMap.put("key1", "value1");
        testPayloadMap.put("key2", 123);
        testPayload = "{\"key1\":\"value1\",\"key2\":123}";
        testToken.setPayload(testPayload);
    }

    @Test
    void createToken_WithUser_ShouldCreateAndSaveToken() {
        // Arrange
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VerificationToken result = tokenService.createToken(
                testUser, 
                TokenType.EMAIL_VALIDATION, 
                Duration.ofHours(24), 
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(TokenType.EMAIL_VALIDATION, result.getTokenType());
        assertFalse(result.isUsed());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());

        // Verify
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        VerificationToken capturedToken = tokenCaptor.getValue();
        assertEquals(testUser, capturedToken.getUser());
        assertEquals(TokenType.EMAIL_VALIDATION, capturedToken.getTokenType());
        assertFalse(capturedToken.isUsed());
    }

    @Test
    void createToken_WithoutUser_ShouldCreateAndSaveToken() {
        // Arrange
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VerificationToken result = tokenService.createToken(
                null, 
                TokenType.TEAM_INVITATION, 
                Duration.ofHours(48), 
                testPayload
        );

        // Assert
        assertNotNull(result);
        assertNull(result.getUser());
        assertEquals(TokenType.TEAM_INVITATION, result.getTokenType());
        assertEquals(testPayload, result.getPayload());
        assertFalse(result.isUsed());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());

        // Verify
        verify(tokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnToken() {
        // Arrange
        when(tokenRepository.findByToken(testTokenString)).thenReturn(Optional.of(testToken));

        // Act
        VerificationToken result = tokenService.validateToken(testTokenString, TokenType.EMAIL_VALIDATION);

        // Assert
        assertNotNull(result);
        assertEquals(testToken, result);

        // Verify
        verify(tokenRepository).findByToken(testTokenString);
    }

    @Test
    void validateToken_WithNonExistentToken_ShouldThrowInvalidTokenException() {
        // Arrange
        when(tokenRepository.findByToken(testTokenString)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> tokenService.validateToken(testTokenString, TokenType.EMAIL_VALIDATION)
        );
        assertEquals("Token non trouvé.", exception.getMessage());

        // Verify
        verify(tokenRepository).findByToken(testTokenString);
    }

    @Test
    void validateToken_WithUsedToken_ShouldThrowInvalidTokenException() {
        // Arrange
        testToken.setUsed(true);
        when(tokenRepository.findByToken(testTokenString)).thenReturn(Optional.of(testToken));

        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> tokenService.validateToken(testTokenString, TokenType.EMAIL_VALIDATION)
        );
        assertEquals("Ce token a déjà été utilisé.", exception.getMessage());

        // Verify
        verify(tokenRepository).findByToken(testTokenString);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldThrowInvalidTokenException() {
        // Arrange
        testToken.setExpiryDate(Instant.now().minus(Duration.ofHours(1)));
        when(tokenRepository.findByToken(testTokenString)).thenReturn(Optional.of(testToken));

        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> tokenService.validateToken(testTokenString, TokenType.EMAIL_VALIDATION)
        );
        assertEquals("Ce token a expiré.", exception.getMessage());

        // Verify
        verify(tokenRepository).findByToken(testTokenString);
    }

    @Test
    void validateToken_WithIncorrectType_ShouldThrowInvalidTokenException() {
        // Arrange
        when(tokenRepository.findByToken(testTokenString)).thenReturn(Optional.of(testToken));

        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> tokenService.validateToken(testTokenString, TokenType.PASSWORD_RESET)
        );
        assertEquals("Le type de token est incorrect.", exception.getMessage());

        // Verify
        verify(tokenRepository).findByToken(testTokenString);
    }

    @Test
    void markTokenAsUsed_ShouldUpdateAndSaveToken() {
        // Arrange
        when(tokenRepository.save(testToken)).thenReturn(testToken);

        // Act
        tokenService.markTokenAsUsed(testToken);

        // Assert
        assertTrue(testToken.isUsed());

        // Verify
        verify(tokenRepository).save(testToken);
    }

    @Test
    void getPayload_WithValidPayload_ShouldReturnParsedMap() throws JsonProcessingException {
        // Arrange
        when(objectMapper.readValue(eq(testPayload), any(TypeReference.class))).thenReturn(testPayloadMap);

        // Act
        Map<String, Object> result = tokenService.getPayload(testToken);

        // Assert
        assertNotNull(result);
        assertEquals(testPayloadMap, result);
        assertEquals("value1", result.get("key1"));
        assertEquals(123, result.get("key2"));

        // Verify
        verify(objectMapper).readValue(eq(testPayload), any(TypeReference.class));
    }

    @Test
    void getPayload_WithNullPayload_ShouldReturnEmptyMap() {
        // Arrange
        testToken.setPayload(null);

        // Act
        Map<String, Object> result = tokenService.getPayload(testToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verifyNoInteractions(objectMapper);
    }

    @Test
    void getPayload_WithEmptyPayload_ShouldReturnEmptyMap() {
        // Arrange
        testToken.setPayload("");

        // Act
        Map<String, Object> result = tokenService.getPayload(testToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verifyNoInteractions(objectMapper);
    }

    @Test
    void getPayload_WithInvalidJson_ShouldThrowInvalidTokenException() throws JsonProcessingException {
        // Arrange
        when(objectMapper.readValue(eq(testPayload), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> tokenService.getPayload(testToken)
        );
        assertEquals("Impossible d'analyser le payload du token.", exception.getMessage());

        // Verify
        verify(objectMapper).readValue(eq(testPayload), any(TypeReference.class));
    }
}
