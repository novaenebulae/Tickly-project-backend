package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.statistics.EventTicketStatisticsDto;
import edu.cda.project.ticklybackend.dtos.statistics.StructureDashboardStatsDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.statistics.StatisticsRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.security.OrganizationalSecurityService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private OrganizationalSecurityService organizationalSecurityService;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private User mockUser;
    private Event mockEvent;
    private Structure mockStructure;
    private final Long structureId = 1L;
    private final Long eventId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // Setup mock user
        mockUser = new User();
        mockUser.setId(userId);

        // Setup mock structure
        mockStructure = new Structure();
        mockStructure.setId(structureId);
        mockStructure.setName("Test Structure");

        // Setup mock event
        mockEvent = new Event();
        mockEvent.setId(eventId);
        mockEvent.setName("Test Event");
        mockEvent.setStructure(mockStructure);
    }

    @Test
    void getStructureDashboardStats_WhenAuthorized_ReturnsStats() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(organizationalSecurityService.canAccessStructure(eq(structureId), any(Authentication.class))).thenReturn(true);

        // Mock repository methods for chart data
        List<Map<String, Object>> topEvents = new ArrayList<>();
        Map<String, Object> event = new HashMap<>();
        event.put("name", "Event 1");
        event.put("ticket_count", 100L); // Use Long for counts
        topEvents.add(event);
        when(statisticsRepository.findTopEventsByTickets(eq(structureId), anyInt())).thenReturn(topEvents);

        List<Map<String, Object>> attendanceByCategory = new ArrayList<>();
        Map<String, Object> category = new HashMap<>();
        category.put("name", "Category 1");
        category.put("attendee_count", 50L); // Use Long for counts
        attendanceByCategory.add(category);
        when(statisticsRepository.findAttendanceByCategory(structureId)).thenReturn(attendanceByCategory);

        // Act
        StructureDashboardStatsDto result = statisticsService.getStructureDashboardStats(structureId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTopEventsChart());
        assertNotNull(result.getAttendanceByCategoryChart());
        assertEquals("bar", result.getTopEventsChart().getChartType());
        assertEquals("doughnut", result.getAttendanceByCategoryChart().getChartType());

        // Verify repository methods were called
        verify(statisticsRepository).findTopEventsByTickets(eq(structureId), anyInt());
        verify(statisticsRepository).findAttendanceByCategory(structureId);
    }

    @Test
    void getEventStats_WhenEventNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> statisticsService.getEventStats(eventId));

        // Verify repository methods were not called
        verify(statisticsRepository, never()).findZoneFillRatesByEventId(anyLong());
        verify(statisticsRepository, never()).findReservationsByDay(anyLong());
        verify(statisticsRepository, never()).findTicketStatusDistribution(anyLong());
    }
    
    @Test
    void getStructureDashboardStats_WhenNoTickets_ReturnsZeroValues() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(organizationalSecurityService.canAccessStructure(eq(structureId), any(Authentication.class))).thenReturn(true);

        // Mock repository methods to return events but no tickets
        when(eventRepository.countByStructureIdAndStartDateAfterAndDeletedFalse(eq(structureId), any(Instant.class))).thenReturn(5L);
        when(ticketRepository.countByEventStructureIdAndStatusIn(eq(structureId), any(Collection.class))).thenReturn(0L);
        when(ticketRepository.countByEventStructureIdAndEventStartDateAfterAndStatus(eq(structureId), any(Instant.class), any(TicketStatus.class))).thenReturn(0L);

        // Mock past events with no tickets
        List<Event> pastEvents = new ArrayList<>();
        Event pastEvent = new Event();
        pastEvent.setId(2L);
        pastEvent.setName("Past Event");
        pastEvents.add(pastEvent);
        when(eventRepository.findByStructureIdAndEndDateBeforeAndDeletedFalse(eq(structureId), any(Instant.class))).thenReturn(pastEvents);
        when(ticketRepository.countByEventIdAndStatus(anyLong(), any(TicketStatus.class))).thenReturn(0L);
        when(ticketRepository.countByEventIdAndStatusIn(anyLong(), any(Collection.class))).thenReturn(0L);

        // Mock empty chart data
        when(statisticsRepository.findTopEventsByTickets(eq(structureId), anyInt())).thenReturn(Collections.emptyList());
        when(statisticsRepository.findAttendanceByCategory(structureId)).thenReturn(Collections.emptyList());

        // Act
        StructureDashboardStatsDto result = statisticsService.getStructureDashboardStats(structureId);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getUpcomingEventsCount());
        assertEquals(0, result.getTotalTicketsReserved());
        assertEquals(0, result.getTotalExpectedAttendees());
        assertEquals(0.0, result.getAverageAttendanceRate());
    }

    @Test
    void calculateAverageAttendanceRate_WhenPastEventsWithTickets_ReturnsCorrectRate() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(organizationalSecurityService.canAccessStructure(eq(structureId), any(Authentication.class))).thenReturn(true);

        // Mock past events with tickets
        List<Event> pastEvents = new ArrayList<>();
        Event pastEvent1 = new Event();
        pastEvent1.setId(2L);
        pastEvent1.setName("Past Event 1");

        Event pastEvent2 = new Event();
        pastEvent2.setId(3L);
        pastEvent2.setName("Past Event 2");

        pastEvents.add(pastEvent1);
        pastEvents.add(pastEvent2);

        when(eventRepository.findByStructureIdAndEndDateBeforeAndDeletedFalse(eq(structureId), any(Instant.class))).thenReturn(pastEvents);

        // Event 1: 80 used tickets out of 100 total (80% attendance)
        when(ticketRepository.countByEventIdAndStatus(eq(2L), eq(TicketStatus.USED))).thenReturn(80L);
        when(ticketRepository.countByEventIdAndStatusIn(eq(2L), eq(Arrays.asList(TicketStatus.VALID, TicketStatus.USED)))).thenReturn(100L);

        // Event 2: 60 used tickets out of 100 total (60% attendance)
        when(ticketRepository.countByEventIdAndStatus(eq(3L), eq(TicketStatus.USED))).thenReturn(60L);
        when(ticketRepository.countByEventIdAndStatusIn(eq(3L), eq(Arrays.asList(TicketStatus.VALID, TicketStatus.USED)))).thenReturn(100L);

        // Mock other repository methods
        when(eventRepository.countByStructureIdAndStartDateAfterAndDeletedFalse(eq(structureId), any(Instant.class))).thenReturn(0L);
        when(ticketRepository.countByEventStructureIdAndStatusIn(eq(structureId), any(Collection.class))).thenReturn(0L);
        when(ticketRepository.countByEventStructureIdAndEventStartDateAfterAndStatus(eq(structureId), any(Instant.class), any(TicketStatus.class))).thenReturn(0L);
        when(statisticsRepository.findTopEventsByTickets(eq(structureId), anyInt())).thenReturn(Collections.emptyList());
        when(statisticsRepository.findAttendanceByCategory(structureId)).thenReturn(Collections.emptyList());

        // Act
        StructureDashboardStatsDto result = statisticsService.getStructureDashboardStats(structureId);

        // Assert
        assertNotNull(result);
        // Average of 80% and 60% is 70%
        assertEquals(70.0, result.getAverageAttendanceRate());
    }

    @Test
    void getEventTicketStats_WhenEventExists_ReturnsCorrectStats() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));

        // Mock repository methods for ticket counts
        when(ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.VALID)).thenReturn(80L);
        when(ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.USED)).thenReturn(20L);

        // Act
        EventTicketStatisticsDto result = statisticsService.getEventTicketStats(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals(mockEvent.getName(), result.getEventName());
        assertEquals(100L, result.getTotalTickets());
        assertEquals(20L, result.getScannedTickets());
        assertEquals(80L, result.getRemainingTickets());
        assertEquals(20.0, result.getFillRate()); // 20 used out of 100 total = 20%

        // Verify repository methods were called
        verify(ticketRepository).countByEventIdAndStatus(eventId, TicketStatus.VALID);
        verify(ticketRepository).countByEventIdAndStatus(eventId, TicketStatus.USED);
    }

    @Test
    void getEventTicketStats_WhenEventNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> statisticsService.getEventTicketStats(eventId));

        // Verify repository methods were not called
        verify(ticketRepository, never()).countByEventIdAndStatus(anyLong(), any(TicketStatus.class));
    }

    @Test
    void getEventTicketStats_WhenNoTickets_ReturnsZeroValues() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));

        // Mock repository methods to return zero tickets
        when(ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.VALID)).thenReturn(0L);
        when(ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.USED)).thenReturn(0L);

        // Act
        EventTicketStatisticsDto result = statisticsService.getEventTicketStats(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals(mockEvent.getName(), result.getEventName());
        assertEquals(0L, result.getTotalTickets());
        assertEquals(0L, result.getScannedTickets());
        assertEquals(0L, result.getRemainingTickets());
        assertEquals(0.0, result.getFillRate());

        // Verify repository methods were called
        verify(ticketRepository).countByEventIdAndStatus(eventId, TicketStatus.VALID);
        verify(ticketRepository).countByEventIdAndStatus(eventId, TicketStatus.USED);
    }
}
