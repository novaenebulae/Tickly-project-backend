package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.exceptions.TokenRefreshException;
import edu.cda.project.ticklybackend.models.token.RefreshToken;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.token.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User testUser;
    private RefreshToken validRefreshToken;
    private RefreshToken expiredRefreshToken;
    private RefreshToken revokedRefreshToken;

    @BeforeEach
    void setUp() {
        // Set refresh token duration
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L); // 1 day

        // Create test user
        testUser = new SpectatorUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Create valid refresh token
        validRefreshToken = new RefreshToken();
        validRefreshToken.setId(1L);
        validRefreshToken.setUser(testUser);
        validRefreshToken.setToken("hashed-valid-token");
        validRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600)); // 1 hour in the future
        validRefreshToken.setRevoked(false);

        // Create expired refresh token
        expiredRefreshToken = new RefreshToken();
        expiredRefreshToken.setId(2L);
        expiredRefreshToken.setUser(testUser);
        expiredRefreshToken.setToken("hashed-expired-token");
        expiredRefreshToken.setExpiryDate(Instant.now().minusSeconds(3600)); // 1 hour in the past
        expiredRefreshToken.setRevoked(false);

        // Create revoked refresh token
        revokedRefreshToken = new RefreshToken();
        revokedRefreshToken.setId(3L);
        revokedRefreshToken.setUser(testUser);
        revokedRefreshToken.setToken("hashed-revoked-token");
        revokedRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600)); // 1 hour in the future
        revokedRefreshToken.setRevoked(true);
    }

    @Test
    void createRefreshToken_ShouldCreateAndSaveToken() {
        // Arrange
        String rawToken = "raw-token";
        String hashedToken = "hashed-token";
        
        when(passwordEncoder.encode(anyString())).thenReturn(hashedToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        String result = refreshTokenService.createRefreshToken(testUser);
        
        // Assert
        assertNotNull(result);
        
        // Verify
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        
        RefreshToken savedToken = tokenCaptor.getValue();
        assertEquals(testUser, savedToken.getUser());
        assertEquals(hashedToken, savedToken.getToken());
        assertFalse(savedToken.isRevoked());
        assertNotNull(savedToken.getExpiryDate());
    }

    @Test
    void verifyExpiration_WithValidToken_ShouldReturnToken() {
        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(validRefreshToken);
        
        // Assert
        assertNotNull(result);
        assertEquals(validRefreshToken, result);
        
        // Verify
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void verifyExpiration_WithExpiredToken_ShouldThrowException() {
        // Act & Assert
        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.verifyExpiration(expiredRefreshToken);
        });
        
        // Verify
        verify(refreshTokenRepository).delete(expiredRefreshToken);
    }

    @Test
    void findByToken_WithExistingToken_ShouldReturnToken() {
        // Arrange
        String rawToken = "raw-token";
        List<RefreshToken> allTokens = Arrays.asList(validRefreshToken, expiredRefreshToken, revokedRefreshToken);
        
        when(refreshTokenRepository.findAll()).thenReturn(allTokens);
        when(passwordEncoder.matches(eq(rawToken), eq(validRefreshToken.getToken()))).thenReturn(true);
        // We don't need to mock the other matches calls since the stream will stop after finding the first match
        
        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(rawToken);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(validRefreshToken, result.get());
        
        // Verify
        verify(refreshTokenRepository).findAll();
        // Only one call to matches() is expected because the stream stops after finding the first match
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void findByToken_WithNonExistingToken_ShouldReturnEmpty() {
        // Arrange
        String rawToken = "non-existing-token";
        List<RefreshToken> allTokens = Arrays.asList(validRefreshToken, expiredRefreshToken, revokedRefreshToken);
        
        when(refreshTokenRepository.findAll()).thenReturn(allTokens);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(rawToken);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(refreshTokenRepository).findAll();
        // In this case, all tokens are checked because none match
        verify(passwordEncoder, times(3)).matches(anyString(), anyString());
    }

    @Test
    void revokeToken_ShouldSetRevokedToTrue() {
        // Arrange
        RefreshToken tokenToRevoke = new RefreshToken();
        tokenToRevoke.setId(1L);
        tokenToRevoke.setUser(testUser);
        tokenToRevoke.setToken("hashed-token");
        tokenToRevoke.setExpiryDate(Instant.now().plusSeconds(3600));
        tokenToRevoke.setRevoked(false);
        
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        refreshTokenService.revokeToken(tokenToRevoke);
        
        // Assert
        assertTrue(tokenToRevoke.isRevoked());
        
        // Verify
        verify(refreshTokenRepository).save(tokenToRevoke);
    }

    @Test
    void revokeAllUserTokens_ShouldDeleteAllUserTokens() {
        // Arrange
        doNothing().when(refreshTokenRepository).deleteAllByUser(testUser);
        
        // Act
        refreshTokenService.revokeAllUserTokens(testUser);
        
        // Verify
        verify(refreshTokenRepository).deleteAllByUser(testUser);
    }

    @Test
    void deleteExpiredTokens_ShouldDeleteExpiredAndRevokedTokens() {
        // Arrange
        List<RefreshToken> expiredOrRevokedTokens = Arrays.asList(expiredRefreshToken, revokedRefreshToken);
        
        when(refreshTokenRepository.findAllExpiredOrRevoked(any(Instant.class))).thenReturn(expiredOrRevokedTokens);
        doNothing().when(refreshTokenRepository).deleteAllExpiredOrRevoked(any(Instant.class));
        
        // Act
        refreshTokenService.deleteExpiredTokens();
        
        // Verify
        verify(refreshTokenRepository).findAllExpiredOrRevoked(any(Instant.class));
        verify(refreshTokenRepository).deleteAllExpiredOrRevoked(any(Instant.class));
    }
}