package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.event.EventMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventCategory;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventCategoryRepository;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.structure.AudienceZoneTemplateRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.security.EventSecurityService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import edu.cda.project.ticklybackend.utils.EventStatusUpdateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventCategoryRepository categoryRepository;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private AudienceZoneTemplateRepository templateRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EventStatusUpdateUtils eventStatusUpdateService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private EventSecurityService eventSecurityService;

    @InjectMocks
    private EventServiceImpl eventService;

    private User testUser;
    private Structure testStructure;
    private Event testEvent;
    private EventCreationDto creationDto;
    private EventDetailResponseDto detailResponseDto;
    private EventUpdateDto updateDto;
    private EventStatusUpdateDto statusUpdateDto;
    private EventSearchParamsDto searchParamsDto;
    private List<EventCategory> testCategories;
    private Long eventId;

    @BeforeEach
    void setUp() {
        // Set up common test data
        eventId = 1L;

        // Create test user
        testUser = new SpectatorUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Create test structure
        testStructure = new Structure();
        testStructure.setId(1L);
        testStructure.setName("Test Structure");

        // Create test categories
        EventCategory category1 = new EventCategory();
        category1.setId(1L);
        category1.setName("Category 1");

        EventCategory category2 = new EventCategory();
        category2.setId(2L);
        category2.setName("Category 2");

        testCategories = Arrays.asList(category1, category2);

        // Create test event
        testEvent = new Event();
        testEvent.setId(eventId);
        testEvent.setName("Test Event");
        testEvent.setStatus(EventStatus.DRAFT);
        testEvent.setCreator(testUser);
        testEvent.setStructure(testStructure);
        testEvent.setCategories(new HashSet<>(testCategories));
        testEvent.setStartDate(Instant.now().plusSeconds(86400)); // tomorrow
        testEvent.setEndDate(Instant.now().plusSeconds(172800)); // day after tomorrow
        testEvent.setDeleted(false);

        // Create event creation DTO
        creationDto = new EventCreationDto();
        creationDto.setName("Test Event");
        creationDto.setStructureId(1L);
        creationDto.setStartDate(ZonedDateTime.now().plusDays(1));
        creationDto.setEndDate(ZonedDateTime.now().plusDays(2));
        creationDto.setCategoryIds(Arrays.asList(1L, 2L));
        creationDto.setAudienceZones(new ArrayList<>());

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
    }

    @Test
    void createEvent_WithValidData_ShouldReturnCreatedEvent() {
        // Arrange
        // Add audience zone configuration to the creation DTO
        EventAudienceZoneConfigDto zoneConfigDto = new EventAudienceZoneConfigDto();
        zoneConfigDto.setTemplateId(1L);
        zoneConfigDto.setAllocatedCapacity(100);
        creationDto.setAudienceZones(Collections.singletonList(zoneConfigDto));

        // Create a properly configured mock for AudienceZoneTemplate
        edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate template = mock(edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate.class);
        edu.cda.project.ticklybackend.models.structure.StructureArea area = mock(edu.cda.project.ticklybackend.models.structure.StructureArea.class);

        // Set up the mocks
        when(template.getMaxCapacity()).thenReturn(200);
        when(template.getArea()).thenReturn(area);
        when(area.getStructure()).thenReturn(testStructure);
        when(area.getId()).thenReturn(1L);

        // Mock the audience zone template repository
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateRepository.findAllById(Collections.singleton(1L))).thenReturn(Collections.singletonList(template));

        // Mock no conflicting events
        when(eventRepository.findConflictingEvents(
                eq(1L), // structureId
                any(Instant.class), // startDate
                any(Instant.class), // endDate
                eq(Collections.singleton(1L)), // areaIds
                eq(null) // excludeEventId
        )).thenReturn(Collections.emptyList());

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(structureRepository.findById(1L)).thenReturn(Optional.of(testStructure));
        when(categoryRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(testCategories);
        when(eventMapper.toEntity(creationDto)).thenReturn(testEvent);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toDetailDto(testEvent)).thenReturn(detailResponseDto);

        // Act
        EventDetailResponseDto result = eventService.createEvent(creationDto);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Test Event", result.getName());
        assertEquals(EventStatus.DRAFT, result.getStatus());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(structureRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findAllById(Arrays.asList(1L, 2L));
        verify(templateRepository, times(1)).findAllById(Collections.singleton(1L));
        verify(eventRepository, times(1)).findConflictingEvents(
                eq(1L), // structureId
                any(Instant.class), // startDate
                any(Instant.class), // endDate
                eq(Collections.singleton(1L)), // areaIds
                eq(null) // excludeEventId
        );
        verify(eventMapper, times(1)).toEntity(creationDto);
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(eventMapper, times(1)).toDetailDto(testEvent);
    }

    @Test
    void createEvent_WithInvalidStructure_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(structureRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.createEvent(creationDto);
        });

        // Verify
        verify(structureRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_WithInvalidDateRange_ShouldThrowBadRequestException() {
        // Arrange
        creationDto.setEndDate(ZonedDateTime.now().minusDays(1)); // End date before start date

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            eventService.createEvent(creationDto);
        });

        // Verify
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_WithConflictingEvents_ShouldThrowBadRequestException() {
        // Arrange
        // Add audience zone configuration to the creation DTO
        EventAudienceZoneConfigDto zoneConfigDto = new EventAudienceZoneConfigDto();
        zoneConfigDto.setTemplateId(1L);
        zoneConfigDto.setAllocatedCapacity(100);
        creationDto.setAudienceZones(Collections.singletonList(zoneConfigDto));

        // Create a properly configured mock for AudienceZoneTemplate
        edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate template = mock(edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate.class);
        edu.cda.project.ticklybackend.models.structure.StructureArea area = mock(edu.cda.project.ticklybackend.models.structure.StructureArea.class);

        // Set up the mocks - only what's needed for the test
        when(template.getArea()).thenReturn(area);
        when(area.getId()).thenReturn(1L);

        // Mock the audience zone template repository - only what's needed for the test
        when(templateRepository.findAllById(Collections.singleton(1L))).thenReturn(Collections.singletonList(template));

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(structureRepository.findById(1L)).thenReturn(Optional.of(testStructure));
        when(categoryRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(testCategories);

        // Create a conflicting event
        Event conflictingEvent = new Event();
        conflictingEvent.setId(2L);
        conflictingEvent.setName("Conflicting Event");
        conflictingEvent.setStatus(EventStatus.PUBLISHED);

        // Mock the repository to return a conflicting event
        when(eventRepository.findConflictingEvents(
                eq(1L), // structureId
                any(Instant.class), // startDate
                any(Instant.class), // endDate
                eq(Collections.singleton(1L)), // areaIds
                eq(null) // excludeEventId
        )).thenReturn(Collections.singletonList(conflictingEvent));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            eventService.createEvent(creationDto);
        });

        // Verify the exception message contains information about the conflict
        String exceptionMessage = exception.getMessage();
        System.out.println("[DEBUG_LOG] Exception message: " + exceptionMessage);

        assertTrue(exceptionMessage.contains("Impossible de créer l'événement"),
                "Exception message should mention inability to create event");
        assertTrue(exceptionMessage.contains("conflit"),
                "Exception message should mention conflict");
        assertTrue(exceptionMessage.contains("Conflicting Event"),
                "Exception message should include the name of the conflicting event");

        // Verify
        verify(templateRepository, times(1)).findAllById(Collections.singleton(1L));
        verify(eventRepository, times(1)).findConflictingEvents(
                eq(1L), // structureId
                any(Instant.class), // startDate
                any(Instant.class), // endDate
                eq(Collections.singleton(1L)), // areaIds
                eq(null) // excludeEventId
        );
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void getEventById_WithValidId_ShouldReturnEventDetails() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventSecurityService.canAccessEventDetails(eq(eventId), any(), any(), anyBoolean())).thenReturn(true);
        when(eventMapper.toDetailDto(testEvent)).thenReturn(detailResponseDto);

        // Act
        EventDetailResponseDto result = eventService.getEventById(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Test Event", result.getName());
        assertEquals(EventStatus.DRAFT, result.getStatus());

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventSecurityService, times(1)).canAccessEventDetails(eq(eventId), any(), any(), anyBoolean());
        verify(eventMapper, times(1)).toDetailDto(testEvent);
    }

    @Test
    void getEventById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());
        when(eventRepository.findByIdIncludingDeleted(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.getEventById(eventId);
        });

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).findByIdIncludingDeleted(eventId);
    }

    @Test
    void getEventById_WithDeletedEvent_ShouldThrowResourceNotFoundException() {
        // Arrange
        testEvent.setDeleted(true);
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());
        when(eventRepository.findByIdIncludingDeleted(eventId)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.getEventById(eventId);
        });

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).findByIdIncludingDeleted(eventId);
    }

    @Test
    void updateEventStatus_FromDraftToPublished_ShouldUpdateStatus() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toDetailDto(testEvent)).thenReturn(detailResponseDto);

        // Act
        EventDetailResponseDto result = eventService.updateEventStatus(eventId, statusUpdateDto);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals(EventStatus.DRAFT, result.getStatus()); // The mock returns the original DTO

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).save(testEvent);
        verify(eventMapper, times(1)).toDetailDto(testEvent);
    }

    @Test
    void updateEventStatus_FromPublishedToNonCancelled_ShouldThrowBadRequestException() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        statusUpdateDto.setStatus(EventStatus.DRAFT); // Trying to change from PUBLISHED to DRAFT
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            eventService.updateEventStatus(eventId, statusUpdateDto);
        });

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void searchEvents_ShouldReturnPaginatedEvents() {
        // Arrange
        List<Event> events = Collections.singletonList(testEvent);
        Page<Event> eventPage = new PageImpl<>(events);

        // Fix: Use a different approach to avoid matcher issues
        doAnswer(invocation -> {
            Specification<Event> spec = invocation.getArgument(0);
            return spec;
        }).when(eventSecurityService).addSecurityFilters(any());

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(eventPage);
        when(eventMapper.toSummaryDto(testEvent)).thenReturn(new EventSummaryDto());

        // Act
        PaginatedResponseDto<EventSummaryDto> result = eventService.searchEvents(searchParamsDto, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalItems());

        // Verify
        verify(eventSecurityService, times(1)).addSecurityFilters(any());
        verify(eventRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(eventMapper, times(1)).toSummaryDto(testEvent);
    }

    @Test
    void deleteEvent_WithDraftEvent_ShouldMarkAsDeleted() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);

        // Act
        eventService.deleteEvent(eventId);

        // Assert
        assertTrue(testEvent.isDeleted());

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void deleteEvent_WithPublishedEvent_ShouldThrowBadRequestException() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            eventService.deleteEvent(eventId);
        });

        // Verify
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }
}
