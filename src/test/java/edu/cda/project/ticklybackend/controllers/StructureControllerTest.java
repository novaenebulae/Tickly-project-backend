package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.structure.*;
import edu.cda.project.ticklybackend.services.interfaces.StructureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StructureControllerTest {

    @Mock
    private StructureService structureService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StructureController structureController;

    private StructureCreationDto creationDto;
    private StructureCreationResponseDto creationResponseDto;
    private StructureUpdateDto updateDto;
    private StructureDetailResponseDto detailResponseDto;
    private StructureSummaryDto summaryDto;
    private AreaCreationDto areaCreationDto;
    private AreaUpdateDto areaUpdateDto;
    private AreaResponseDto areaResponseDto;
    private AudienceZoneTemplateCreationDto templateCreationDto;
    private AudienceZoneTemplateUpdateDto templateUpdateDto;
    private AudienceZoneTemplateResponseDto templateResponseDto;
    private Long structureId;
    private Long areaId;
    private Long templateId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up common test data
        structureId = 1L;
        areaId = 1L;
        templateId = 1L;

        // Create structure creation DTO
        creationDto = new StructureCreationDto();
        creationDto.setName("Test Structure");
        creationDto.setTypeIds(Arrays.asList(1L, 2L));

        AddressDto addressDto = new AddressDto();
        addressDto.setStreet("123 Test Street");
        addressDto.setCity("Test City");
        addressDto.setZipCode("12345");
        addressDto.setCountry("Test Country");
        creationDto.setAddress(addressDto);

        // Create structure creation response DTO
        creationResponseDto = new StructureCreationResponseDto();
        creationResponseDto.setId(structureId);
        creationResponseDto.setName("Test Structure");

        // Create structure update DTO
        updateDto = new StructureUpdateDto();
        updateDto.setName("Updated Structure");
        updateDto.setDescription("Updated description");

        // Create structure detail response DTO
        detailResponseDto = new StructureDetailResponseDto();
        detailResponseDto.setId(structureId);
        detailResponseDto.setName("Test Structure");
        detailResponseDto.setDescription("Test description");

        // Create structure summary DTO
        summaryDto = new StructureSummaryDto();
        summaryDto.setId(structureId);
        summaryDto.setName("Test Structure");

        // Create area creation DTO
        areaCreationDto = new AreaCreationDto();
        areaCreationDto.setName("Test Area");
        areaCreationDto.setDescription("Test area description");
        areaCreationDto.setMaxCapacity(100);

        // Create area update DTO
        areaUpdateDto = new AreaUpdateDto();
        areaUpdateDto.setName("Updated Area");
        areaUpdateDto.setDescription("Updated area description");

        // Create area response DTO
        areaResponseDto = new AreaResponseDto();
        areaResponseDto.setId(areaId);
        areaResponseDto.setName("Test Area");
        areaResponseDto.setDescription("Test area description");

        // Create audience zone template creation DTO
        templateCreationDto = new AudienceZoneTemplateCreationDto();
        templateCreationDto.setName("Test Template");
        templateCreationDto.setMaxCapacity(50);

        // Create audience zone template update DTO
        templateUpdateDto = new AudienceZoneTemplateUpdateDto();
        templateUpdateDto.setName("Updated Template");

        // Create audience zone template response DTO
        templateResponseDto = new AudienceZoneTemplateResponseDto();
        templateResponseDto.setId(templateId);
        templateResponseDto.setName("Test Template");
    }

    @Test
    void createStructure_ShouldReturnCreatedStructure() {
        // Arrange
        edu.cda.project.ticklybackend.models.user.User mockUser = new edu.cda.project.ticklybackend.models.user.User();
        mockUser.setEmail("test@example.com");

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(structureService.createStructure(any(StructureCreationDto.class), eq("test@example.com"))).thenReturn(creationResponseDto);

        // Act
        ResponseEntity<StructureCreationResponseDto> response = structureController.createStructure(creationDto, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(structureId, response.getBody().getId());
        assertEquals("Test Structure", response.getBody().getName());

        // Verify
        verify(structureService, times(1)).createStructure(any(StructureCreationDto.class), eq("test@example.com"));
    }

    @Test
    void getAllStructures_ShouldReturnPaginatedStructures() {
        // Arrange
        List<StructureSummaryDto> structures = Collections.singletonList(summaryDto);
        Page<StructureSummaryDto> structurePage = new PageImpl<>(structures);
        when(structureService.getAllStructures(any(Pageable.class), any(StructureSearchParamsDto.class))).thenReturn(structurePage);

        // Act
        ResponseEntity<Page<StructureSummaryDto>> response = structureController.getAllStructures(Pageable.unpaged(), new StructureSearchParamsDto());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        // Verify
        verify(structureService, times(1)).getAllStructures(any(Pageable.class), any(StructureSearchParamsDto.class));
    }

    @Test
    void getStructureById_ShouldReturnStructureDetails() {
        // Arrange
        when(structureService.getStructureById(structureId)).thenReturn(detailResponseDto);

        // Act
        ResponseEntity<StructureDetailResponseDto> response = structureController.getStructureById(structureId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(structureId, response.getBody().getId());
        assertEquals("Test Structure", response.getBody().getName());

        // Verify
        verify(structureService, times(1)).getStructureById(structureId);
    }

    @Test
    void updateStructure_ShouldReturnUpdatedStructure() {
        // Arrange
        when(structureService.updateStructure(eq(structureId), any(StructureUpdateDto.class))).thenReturn(detailResponseDto);

        // Act
        ResponseEntity<StructureDetailResponseDto> response = structureController.updateStructure(structureId, updateDto, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(structureId, response.getBody().getId());

        // Verify
        verify(structureService, times(1)).updateStructure(structureId, updateDto);
    }

    @Test
    void deleteStructure_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(structureService).deleteStructure(structureId);

        // Act
        ResponseEntity<Void> response = structureController.deleteStructure(structureId, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(structureService, times(1)).deleteStructure(structureId);
    }

    @Test
    void updateStructureLogo_ShouldReturnFileUploadResponse() {
        // Arrange
        String fileUrl = "http://example.com/images/logo.jpg";
        FileUploadResponseDto fileUploadResponseDto = new FileUploadResponseDto("logo.jpg", fileUrl, "Logo uploaded successfully");
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("logo.jpg");
        when(structureService.updateStructureLogo(eq(structureId), any(MultipartFile.class))).thenReturn(fileUploadResponseDto);

        // Act
        ResponseEntity<FileUploadResponseDto> response = structureController.updateStructureLogo(structureId, mockFile, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(fileUrl, response.getBody().getFileUrl());
        assertEquals("logo.jpg", response.getBody().getFileName());

        // Verify
        verify(structureService, times(1)).updateStructureLogo(structureId, mockFile);
    }

    @Test
    void removeStructureLogo_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(structureService).removeStructureLogo(structureId);

        // Act
        ResponseEntity<Void> response = structureController.removeStructureLogo(structureId, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(structureService, times(1)).removeStructureLogo(structureId);
    }

    @Test
    void createArea_ShouldReturnCreatedArea() {
        // Arrange
        when(structureService.createArea(eq(structureId), any(AreaCreationDto.class))).thenReturn(areaResponseDto);

        // Act
        ResponseEntity<AreaResponseDto> response = structureController.createArea(structureId, areaCreationDto, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(areaId, response.getBody().getId());
        assertEquals("Test Area", response.getBody().getName());

        // Verify
        verify(structureService, times(1)).createArea(structureId, areaCreationDto);
    }

    @Test
    void getAreasByStructureId_ShouldReturnAreasList() {
        // Arrange
        List<AreaResponseDto> areas = Collections.singletonList(areaResponseDto);
        when(structureService.getAreasByStructureId(structureId)).thenReturn(areas);

        // Act
        ResponseEntity<List<AreaResponseDto>> response = structureController.getAreasByStructureId(structureId, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(areaId, response.getBody().get(0).getId());

        // Verify
        verify(structureService, times(1)).getAreasByStructureId(structureId);
    }

    @Test
    void createAudienceZoneTemplate_ShouldReturnCreatedTemplate() {
        // Arrange
        when(structureService.createAudienceZoneTemplate(eq(structureId), eq(areaId), any(AudienceZoneTemplateCreationDto.class)))
                .thenReturn(templateResponseDto);

        // Act
        ResponseEntity<AudienceZoneTemplateResponseDto> response =
                structureController.createAudienceZoneTemplate(structureId, areaId, templateCreationDto, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(templateId, response.getBody().getId());
        assertEquals("Test Template", response.getBody().getName());

        // Verify
        verify(structureService, times(1)).createAudienceZoneTemplate(structureId, areaId, templateCreationDto);
    }
}
