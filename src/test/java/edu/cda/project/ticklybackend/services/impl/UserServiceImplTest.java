package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.UserMapper;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.user.UserFavoriteStructureRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserFavoriteStructureRepository favoriteRepository;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Structure testStructure;
    private UserProfileResponseDto profileResponseDto;
    private UserProfileUpdateDto profileUpdateDto;
    private UserFavoriteStructure testFavorite;
    private UserFavoriteStructureDto favoriteStructureDto;
    private Long userId;
    private Long structureId;

    @BeforeEach
    void setUp() {
        // Set up common test data
        userId = 1L;
        structureId = 1L;

        // Create test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Create test structure
        testStructure = new Structure();
        testStructure.setId(structureId);
        testStructure.setName("Test Structure");

        // Create profile response DTO
        profileResponseDto = new UserProfileResponseDto();
        profileResponseDto.setId(userId);
        profileResponseDto.setEmail("test@example.com");
        profileResponseDto.setFirstName("Test");
        profileResponseDto.setLastName("User");

        // Create profile update DTO
        profileUpdateDto = new UserProfileUpdateDto();
        profileUpdateDto.setFirstName("Updated");
        profileUpdateDto.setLastName("User");
        profileUpdateDto.setEmail("updated@example.com");

        // Create test favorite
        testFavorite = new UserFavoriteStructure();
        testFavorite.setId(1L);
        testFavorite.setUser(testUser);
        testFavorite.setStructure(testStructure);

        // Create favorite structure DTO
        favoriteStructureDto = new UserFavoriteStructureDto();
        favoriteStructureDto.setId(1L);
        favoriteStructureDto.setUserId(userId);

        // Create structure summary DTO for the favorite
        StructureSummaryDto structureSummaryDto = new StructureSummaryDto();
        structureSummaryDto.setId(structureId);
        structureSummaryDto.setName("Test Structure");

        favoriteStructureDto.setStructure(structureSummaryDto);
    }

    @Test
    void getCurrentUserProfile_ShouldReturnUserProfile() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserProfileResponseDto(testUser)).thenReturn(profileResponseDto);

        // Act
        UserProfileResponseDto result = userService.getCurrentUserProfile();

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(userMapper, times(1)).userToUserProfileResponseDto(testUser);
    }

    @Test
    void updateCurrentUserProfile_ShouldReturnUpdatedProfile() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.userToUserProfileResponseDto(testUser)).thenReturn(profileResponseDto);

        // Act
        UserProfileResponseDto result = userService.updateCurrentUserProfile(profileUpdateDto);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
        verify(userMapper, times(1)).userToUserProfileResponseDto(testUser);
    }

    @Test
    void updateCurrentUserAvatar_ShouldReturnFileUrl() {
        // Arrange
        String fileUrl = "http://example.com/images/avatar.jpg";
        String filePath = "avatar.jpg";
        MultipartFile mockFile = mock(MultipartFile.class);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(fileStorageService.storeFile(any(MultipartFile.class), eq("avatars"))).thenReturn(filePath);
        when(fileStorageService.getFileUrl(filePath, "avatars")).thenReturn(fileUrl);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.updateCurrentUserAvatar(mockFile);

        // Assert
        assertNotNull(result);
        assertEquals(fileUrl, result);

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(userRepository, times(1)).findById(userId);
        verify(fileStorageService, times(1)).storeFile(mockFile, "avatars");
        verify(fileStorageService, times(1)).getFileUrl(filePath, "avatars");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void getCurrentUserFavoriteStructures_ShouldReturnFavoritesList() {
        // Arrange
        List<UserFavoriteStructure> favorites = new ArrayList<>();
        favorites.add(testFavorite);
        List<UserFavoriteStructureDto> favoriteDtos = new ArrayList<>();
        favoriteDtos.add(favoriteStructureDto);

        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(userId);
        when(favoriteRepository.findByUserId(userId)).thenReturn(favorites);
        when(userMapper.userFavoriteStructuresToUserFavoriteStructureDtos(eq(favorites), any(FileStorageService.class))).thenReturn(favoriteDtos);

        // Act
        List<UserFavoriteStructureDto> result = userService.getCurrentUserFavoriteStructures();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(structureId, result.get(0).getStructure().getId());
        assertEquals("Test Structure", result.get(0).getStructure().getName());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(favoriteRepository, times(1)).findByUserId(userId);
        verify(userMapper, times(1)).userFavoriteStructuresToUserFavoriteStructureDtos(eq(favorites), any(FileStorageService.class));
    }

    @Test
    void addCurrentUserFavoriteStructure_ShouldReturnAddedFavorite() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(structureRepository.findById(structureId)).thenReturn(Optional.of(testStructure));
        when(favoriteRepository.existsByUserIdAndStructureId(userId, structureId)).thenReturn(false);
        when(favoriteRepository.save(any(UserFavoriteStructure.class))).thenReturn(testFavorite);
        when(userMapper.userFavoriteStructureToUserFavoriteStructureDto(eq(testFavorite), any(FileStorageService.class))).thenReturn(favoriteStructureDto);

        // Act
        UserFavoriteStructureDto result = userService.addCurrentUserFavoriteStructure(structureId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(structureId, result.getStructure().getId());
        assertEquals("Test Structure", result.getStructure().getName());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(structureRepository, times(1)).findById(structureId);
        verify(favoriteRepository, times(1)).existsByUserIdAndStructureId(userId, structureId);
        verify(favoriteRepository, times(1)).save(any(UserFavoriteStructure.class));
        verify(userMapper, times(1)).userFavoriteStructureToUserFavoriteStructureDto(eq(testFavorite), any(FileStorageService.class));
    }

    @Test
    void addCurrentUserFavoriteStructure_WithExistingFavorite_ShouldThrowBadRequestException() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(structureRepository.findById(structureId)).thenReturn(Optional.of(testStructure));
        when(favoriteRepository.existsByUserIdAndStructureId(userId, structureId)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            userService.addCurrentUserFavoriteStructure(structureId);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(structureRepository, times(1)).findById(structureId);
        verify(favoriteRepository, times(1)).existsByUserIdAndStructureId(userId, structureId);
        verify(favoriteRepository, never()).save(any(UserFavoriteStructure.class));
    }

    @Test
    void removeCurrentUserFavoriteStructure_ShouldRemoveFavorite() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(structureRepository.existsById(structureId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndStructureId(userId, structureId)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteByUserIdAndStructureId(userId, structureId);

        // Act
        userService.removeCurrentUserFavoriteStructure(structureId);

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(userRepository, times(1)).existsById(userId);
        verify(structureRepository, times(1)).existsById(structureId);
        verify(favoriteRepository, times(1)).existsByUserIdAndStructureId(userId, structureId);
        verify(favoriteRepository, times(1)).deleteByUserIdAndStructureId(userId, structureId);
    }

    @Test
    void removeCurrentUserFavoriteStructure_WithNonExistentFavorite_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(structureRepository.existsById(structureId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndStructureId(userId, structureId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.removeCurrentUserFavoriteStructure(structureId);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(userRepository, times(1)).existsById(userId);
        verify(structureRepository, times(1)).existsById(structureId);
        verify(favoriteRepository, times(1)).existsByUserIdAndStructureId(userId, structureId);
        verify(favoriteRepository, never()).deleteByUserIdAndStructureId(any(), any());
    }
}