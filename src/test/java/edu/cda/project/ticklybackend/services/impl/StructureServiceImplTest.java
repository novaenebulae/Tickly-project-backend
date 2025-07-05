package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.structure.AddressDto;
import edu.cda.project.ticklybackend.dtos.structure.StructureCreationDto;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.mappers.structure.StructureMapper;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureTypeRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private StructureServiceImpl structureService;

    private StructureCreationDto creationDto;
    private User validatedUser;
    private User unvalidatedUser;

    @BeforeEach
    void setUp() {
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

        // We don't need to mock these methods for our test since we're expecting an exception
        // before these methods are called for the unvalidated user test
    }

    @Test
    void createStructure_WithValidatedEmail_ShouldSucceed() {
        // Arrange
        when(userRepository.findByEmail("validated@example.com")).thenReturn(Optional.of(validatedUser));

        // For the validated user test, we need to mock these methods
        Structure mockStructure = new Structure();
        when(structureMapper.toEntity(any(StructureCreationDto.class))).thenReturn(mockStructure);
        when(structureRepository.save(any(Structure.class))).thenReturn(mockStructure);

        // Mock a list of structure types with the same size as the typeIds list
        StructureType mockType = new StructureType();
        mockType.setId(1L);
        mockType.setName("Test Type");
        doReturn(Collections.singletonList(mockType)).when(structureTypeRepository).findAllById(any());

        // Act
        structureService.createStructure(creationDto, "validated@example.com");

        // No assertion needed - if no exception is thrown, the test passes
    }

    @Test
    void createStructure_WithUnvalidatedEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("unvalidated@example.com")).thenReturn(Optional.of(unvalidatedUser));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            structureService.createStructure(creationDto, "unvalidated@example.com");
        });
    }
}
