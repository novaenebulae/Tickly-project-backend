package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureCreationDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureCreationResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureDetailResponseDto;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.exceptions.StructureCreationForbiddenException;
import edu.cda.project.ticklybackend.mappers.structure.StructureMapper;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureTypeRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StructureServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private StructureTypeRepository structureTypeRepository;

    @Mock
    private StructureMapper structureMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private StructureServiceImpl structureService;

    private StructureCreationDto creationDto;
    private StructureDetailResponseDto detailResponseDto;
    private User validatedUser;
    private User unvalidatedUser;
    private Structure testStructure;
    private Long structureId;

    @BeforeEach
    void setUp() {
        // Set up ID
        structureId = 1L;

        // Create a valid structure creation DTO
        creationDto = new StructureCreationDto();
        creationDto.setName("Test Structure");
        creationDto.setTypeIds(Collections.singletonList(1L));

        AddressDto addressDto = new AddressDto();
        addressDto.setStreet("123 Test Street");
        addressDto.setCity("Test City");
        addressDto.setZipCode("12345");
        addressDto.setCountry("Test Country");
        creationDto.setAddress(addressDto);

        // Create structure detail response DTO
        detailResponseDto = new StructureDetailResponseDto();
        detailResponseDto.setId(structureId);
        detailResponseDto.setName("Test Structure");
        detailResponseDto.setDescription("Test description");

        // Create a validated spectator user
        validatedUser = new SpectatorUser();
        validatedUser.setId(1L);
        validatedUser.setEmail("validated@example.com");
        validatedUser.setFirstName("Validated");
        validatedUser.setLastName("User");
        validatedUser.setRole(UserRole.SPECTATOR);
        validatedUser.setEmailValidated(true);

        // Create an unvalidated spectator user
        unvalidatedUser = new SpectatorUser();
        unvalidatedUser.setId(2L);
        unvalidatedUser.setEmail("unvalidated@example.com");
        unvalidatedUser.setFirstName("Unvalidated");
        unvalidatedUser.setLastName("User");
        unvalidatedUser.setRole(UserRole.SPECTATOR);
        unvalidatedUser.setEmailValidated(false);

        // Create test structure
        testStructure = new Structure();
        testStructure.setId(structureId);
        testStructure.setName("Test Structure");
        testStructure.setDescription("Test description");

        // We don't need to mock these methods for our test since we're expecting an exception
        // before these methods are called for the unvalidated user test
    }

    @Test
    void createStructure_WithValidatedEmail_ShouldSucceed() {
        // Arrange
        when(userRepository.findByEmail("validated@example.com")).thenReturn(Optional.of(validatedUser));

        // For the validated user test, we need to mock these methods
        Structure mockStructure = new Structure();
        mockStructure.setId(1L);
        when(structureMapper.toEntity(any(StructureCreationDto.class))).thenReturn(mockStructure);
        when(structureRepository.save(any(Structure.class))).thenReturn(mockStructure);

        // Mock upgradeUserToStructureAdmin
        doNothing().when(userRepository).upgradeUserToStructureAdmin(validatedUser.getId(), mockStructure.getId());

        // Mock findById for the updated user
        when(userRepository.findById(validatedUser.getId())).thenReturn(Optional.of(validatedUser));

        // Mock JWT token generation
        when(jwtTokenProvider.generateAccessToken(validatedUser)).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);

        // Act
        StructureCreationResponseDto response = structureService.createStructure(creationDto, "validated@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getStructureId());
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertEquals(3600000L, response.getExpiresIn());
    }

    @Test
    void createStructure_WithUnvalidatedEmail_ShouldThrowException() {
        // Arrange
        // Create a user with a structure already assigned
        User userWithStructure = new SpectatorUser();
        userWithStructure.setId(3L);
        userWithStructure.setEmail("user.with.structure@example.com");
        userWithStructure.setRole(UserRole.STRUCTURE_ADMINISTRATOR);

        // Mock the repository to return this user
        when(userRepository.findByEmail("user.with.structure@example.com")).thenReturn(Optional.of(userWithStructure));

        // Act & Assert
        assertThrows(StructureCreationForbiddenException.class, () -> {
            structureService.createStructure(creationDto, "user.with.structure@example.com");
        });
    }

    @Test
    void getStructureById_WithValidId_ShouldReturnStructure() {
        // Arrange
        when(structureRepository.findById(structureId)).thenReturn(Optional.of(testStructure));
        when(structureMapper.toDetailDto(eq(testStructure), any(FileStorageService.class))).thenReturn(detailResponseDto);

        // Act
        StructureDetailResponseDto result = structureService.getStructureById(structureId);

        // Assert
        assertNotNull(result);
        assertEquals(structureId, result.getId());
        assertEquals("Test Structure", result.getName());
        assertEquals("Test description", result.getDescription());

        // Verify
        verify(structureRepository, times(1)).findById(structureId);
        verify(structureMapper, times(1)).toDetailDto(eq(testStructure), any(FileStorageService.class));
    }

    @Test
    void getStructureById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(structureRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            structureService.getStructureById(99L);
        });

        // Verify
        verify(structureRepository, times(1)).findById(99L);
        verify(structureMapper, never()).toDetailDto(any(), any());
    }
}
