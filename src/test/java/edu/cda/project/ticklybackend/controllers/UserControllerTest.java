package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
import edu.cda.project.ticklybackend.dtos.user.FavoriteStructureRequestDto;
import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import edu.cda.project.ticklybackend.services.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserProfileResponseDto profileResponseDto;
    private UserProfileUpdateDto profileUpdateDto;
    private UserFavoriteStructureDto favoriteStructureDto;
    private FavoriteStructureRequestDto favoriteStructureRequestDto;
    private Long structureId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up common test data
        structureId = 1L;

        // Create profile response DTO
        profileResponseDto = new UserProfileResponseDto();
        profileResponseDto.setId(1L);
        profileResponseDto.setEmail("test@example.com");
        profileResponseDto.setFirstName("Test");
        profileResponseDto.setLastName("User");

        // Create profile update DTO
        profileUpdateDto = new UserProfileUpdateDto();
        profileUpdateDto.setFirstName("Updated");
        profileUpdateDto.setLastName("User");
        profileUpdateDto.setEmail("updated@example.com");

        // Create favorite structure DTO
        favoriteStructureDto = new UserFavoriteStructureDto();
        favoriteStructureDto.setId(1L);
        favoriteStructureDto.setUserId(1L);

        // Create structure summary DTO
        StructureSummaryDto structureSummaryDto = new StructureSummaryDto();
        structureSummaryDto.setId(structureId);
        structureSummaryDto.setName("Test Structure");
        structureSummaryDto.setCity("Test City");
        structureSummaryDto.setActive(true);

        favoriteStructureDto.setStructure(structureSummaryDto);

        // Create favorite structure request DTO
        favoriteStructureRequestDto = new FavoriteStructureRequestDto();
        favoriteStructureRequestDto.setStructureId(structureId);
    }

    @Test
    void getMyProfile_ShouldReturnUserProfile() {
        // Arrange
        when(userService.getCurrentUserProfile()).thenReturn(profileResponseDto);

        // Act
        ResponseEntity<UserProfileResponseDto> response = userController.getMyProfile();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test", response.getBody().getFirstName());
        assertEquals("User", response.getBody().getLastName());

        // Verify
        verify(userService, times(1)).getCurrentUserProfile();
    }

    @Test
    void updateMyProfile_ShouldReturnUpdatedProfile() {
        // Arrange
        when(userService.updateCurrentUserProfile(any(UserProfileUpdateDto.class))).thenReturn(profileResponseDto);

        // Act
        ResponseEntity<UserProfileResponseDto> response = userController.updateMyProfile(profileUpdateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        // Verify
        verify(userService, times(1)).updateCurrentUserProfile(profileUpdateDto);
    }

    @Test
    void uploadMyAvatar_ShouldReturnFileUploadResponse() {
        // Arrange
        String fileUrl = "http://example.com/images/avatar.jpg";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("avatar.jpg");
        when(userService.updateCurrentUserAvatar(any(MultipartFile.class))).thenReturn(fileUrl);

        // Act
        ResponseEntity<FileUploadResponseDto> response = userController.uploadMyAvatar(mockFile);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(fileUrl, response.getBody().getFileUrl());
        assertEquals("avatar.jpg", response.getBody().getFileName());
        assertEquals("Avatar mis à jour avec succès.", response.getBody().getMessage());

        // Verify
        verify(userService, times(1)).updateCurrentUserAvatar(mockFile);
    }

    @Test
    void getMyFavoriteStructures_ShouldReturnFavoriteStructuresList() {
        // Arrange
        List<UserFavoriteStructureDto> favoriteStructures = new ArrayList<>();
        favoriteStructures.add(favoriteStructureDto);
        when(userService.getCurrentUserFavoriteStructures()).thenReturn(favoriteStructures);

        // Act
        ResponseEntity<List<UserFavoriteStructureDto>> response = userController.getMyFavoriteStructures();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(structureId, response.getBody().get(0).getStructure().getId());
        assertEquals("Test Structure", response.getBody().get(0).getStructure().getName());

        // Verify
        verify(userService, times(1)).getCurrentUserFavoriteStructures();
    }

    @Test
    void addMyFavoriteStructure_ShouldReturnAddedFavoriteStructure() {
        // Arrange
        when(userService.addCurrentUserFavoriteStructure(structureId)).thenReturn(favoriteStructureDto);

        // Act
        ResponseEntity<UserFavoriteStructureDto> response = userController.addMyFavoriteStructure(favoriteStructureRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(structureId, response.getBody().getStructure().getId());
        assertEquals("Test Structure", response.getBody().getStructure().getName());

        // Verify
        verify(userService, times(1)).addCurrentUserFavoriteStructure(structureId);
    }

    @Test
    void removeMyFavoriteStructure_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(userService).removeCurrentUserFavoriteStructure(structureId);

        // Act
        ResponseEntity<Void> response = userController.removeMyFavoriteStructure(structureId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(userService, times(1)).removeCurrentUserFavoriteStructure(structureId);
    }

    @Test
    void requestAccountDeletion_ShouldReturnOk() {
        // Arrange
        doNothing().when(userService).requestAccountDeletion();

        // Act
        ResponseEntity<Void> response = userController.requestAccountDeletion();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(userService, times(1)).requestAccountDeletion();
    }

    @Test
    void confirmAccountDeletion_ShouldReturnOk() {
        // Arrange
        String token = "valid-token";
        doNothing().when(userService).confirmAccountDeletion(token);

        // Act
        ResponseEntity<Void> response = userController.confirmAccountDeletion(token);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(userService, times(1)).confirmAccountDeletion(token);
    }
}
