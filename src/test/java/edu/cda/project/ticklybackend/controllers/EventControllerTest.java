package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.services.interfaces.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private EventCreationDto creationDto;
    private EventDetailResponseDto detailResponseDto;
    private EventUpdateDto updateDto;
    private EventStatusUpdateDto statusUpdateDto;
    private EventSearchParamsDto searchParamsDto;
    private PaginatedResponseDto<EventSummaryDto> paginatedResponseDto;
    private List<FriendResponseDto> friendResponseDtos;
    private List<EventCategoryDto> categoryDtos;
    private Long eventId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up common test data
        eventId = 1L;

        // Create event creation DTO
        creationDto = new EventCreationDto();
        creationDto.setName("Test Event");
        creationDto.setStructureId(1L);
        creationDto.setStartDate(ZonedDateTime.now().plusDays(1));
        creationDto.setEndDate(ZonedDateTime.now().plusDays(2));
        creationDto.setCategoryIds(Arrays.asList(1L, 2L));

        // Create event detail response DTO
        detailResponseDto = new EventDetailResponseDto();
        detailResponseDto.setId(eventId);
        detailResponseDto.setName("Test Event");
        detailResponseDto.setStatus(EventStatus.DRAFT);

        // Create event update DTO
        updateDto = new EventUpdateDto();
        updateDto.setName("Updated Event");
        updateDto.setShortDescription("Updated description");

        // Create event status update DTO
        statusUpdateDto = new EventStatusUpdateDto();
        statusUpdateDto.setStatus(EventStatus.PUBLISHED);

        // Create search params DTO
        searchParamsDto = new EventSearchParamsDto();
        searchParamsDto.setQuery("test");

        // Create paginated response DTO
        paginatedResponseDto = new PaginatedResponseDto<>();
        paginatedResponseDto.setTotalItems(1L);
        paginatedResponseDto.setTotalPages(1);
        paginatedResponseDto.setItems(new ArrayList<>());

        // Create friend response DTOs
        friendResponseDtos = new ArrayList<>();

        // Create category DTOs
        categoryDtos = Arrays.asList(
                new EventCategoryDto(1L, "Category 1"),
                new EventCategoryDto(2L, "Category 2")
        );
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() {
        // Arrange
        when(eventService.createEvent(any(EventCreationDto.class))).thenReturn(detailResponseDto);

        // Act
        ResponseEntity<EventDetailResponseDto> response = eventController.createEvent(creationDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getName());
        assertEquals(EventStatus.DRAFT, response.getBody().getStatus());

        // Verify
        verify(eventService, times(1)).createEvent(creationDto);
    }

    @Test
    void searchEvents_ShouldReturnPaginatedEvents() {
        // Arrange
        when(eventService.searchEvents(any(EventSearchParamsDto.class), any(Pageable.class)))
                .thenReturn(paginatedResponseDto);

        // Act
        ResponseEntity<PaginatedResponseDto<EventSummaryDto>> response = 
                eventController.searchEvents(searchParamsDto, Pageable.unpaged());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getTotalItems());
        assertEquals(1, response.getBody().getTotalPages());

        // Verify
        verify(eventService, times(1)).searchEvents(searchParamsDto, Pageable.unpaged());
    }

    @Test
    void getEventById_ShouldReturnEventDetails() {
        // Arrange
        when(eventService.getEventById(eventId)).thenReturn(detailResponseDto);

        // Act
        ResponseEntity<EventDetailResponseDto> response = eventController.getEventById(eventId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getName());

        // Verify
        verify(eventService, times(1)).getEventById(eventId);
    }

    @Test
    void getFriendsAttendingEvent_ShouldReturnFriendsList() {
        // Arrange
        when(eventService.getFriendsAttendingEvent(eventId)).thenReturn(friendResponseDtos);

        // Act
        ResponseEntity<List<FriendResponseDto>> response = eventController.getFriendsAttendingEvent(eventId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(friendResponseDtos.size(), response.getBody().size());

        // Verify
        verify(eventService, times(1)).getFriendsAttendingEvent(eventId);
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() {
        // Arrange
        when(eventService.updateEvent(eq(eventId), any(EventUpdateDto.class))).thenReturn(detailResponseDto);

        // Act
        ResponseEntity<EventDetailResponseDto> response = eventController.updateEvent(eventId, updateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());

        // Verify
        verify(eventService, times(1)).updateEvent(eventId, updateDto);
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(eventService).deleteEvent(eventId);

        // Act
        ResponseEntity<Void> response = eventController.deleteEvent(eventId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(eventService, times(1)).deleteEvent(eventId);
    }

    @Test
    void updateEventStatus_ShouldReturnUpdatedEvent() {
        // Arrange
        when(eventService.updateEventStatus(eq(eventId), any(EventStatusUpdateDto.class))).thenReturn(detailResponseDto);

        // Act
        ResponseEntity<EventDetailResponseDto> response = eventController.updateEventStatus(eventId, statusUpdateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());

        // Verify
        verify(eventService, times(1)).updateEventStatus(eventId, statusUpdateDto);
    }

    @Test
    void uploadMainPhoto_ShouldReturnFileUploadResponse() {
        // Arrange
        String fileUrl = "http://example.com/images/event.jpg";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("event.jpg");
        when(eventService.updateEventMainPhoto(eq(eventId), any(MultipartFile.class))).thenReturn(fileUrl);

        // Act
        ResponseEntity<FileUploadResponseDto> response = eventController.uploadMainPhoto(eventId, mockFile);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(fileUrl, response.getBody().getFileUrl());
        assertEquals("event.jpg", response.getBody().getFileName());

        // Verify
        verify(eventService, times(1)).updateEventMainPhoto(eventId, mockFile);
    }

    @Test
    void getAllCategories_ShouldReturnCategoriesList() {
        // Arrange
        when(eventService.getAllCategories()).thenReturn(categoryDtos);

        // Act
        ResponseEntity<List<EventCategoryDto>> response = eventController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Category 1", response.getBody().get(0).getName());
        assertEquals("Category 2", response.getBody().get(1).getName());

        // Verify
        verify(eventService, times(1)).getAllCategories();
    }
}
