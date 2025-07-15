package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.AbstractIntegrationTest;
import edu.cda.project.ticklybackend.dtos.statistics.EventStatisticsDto;
import edu.cda.project.ticklybackend.dtos.statistics.StructureDashboardStatsDto;
import edu.cda.project.ticklybackend.dtos.statistics.ZoneFillRateDataPointDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.AccessDeniedException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.statistics.StatisticsRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.security.EventSecurityService;
import edu.cda.project.ticklybackend.security.StructureSecurityService;
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
class StatisticsServiceImplTest extends AbstractIntegrationTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private StructureSecurityService structureSecurityService;

    @Mock
    private EventSecurityService eventSecurityService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private User mockUser;
    private Event mockEvent;
    private Structure mockStructure;
    private Long structureId = 1L;
    private Long eventId = 1L;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        // Setup mock user
        mockUser = new SpectatorUser();
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

        when(structureSecurityService.isStructureStaff(eq(structureId), any(Authentication.class))).thenReturn(true);

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
    void getStructureDashboardStats_WhenUnauthorized_ThrowsAccessDeniedException() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(structureSecurityService.isStructureStaff(eq(structureId), any(Authentication.class))).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> statisticsService.getStructureDashboardStats(structureId));

        // Verify repository methods were not called
        verify(statisticsRepository, never()).findTopEventsByTickets(anyLong(), anyInt());
        verify(statisticsRepository, never()).findAttendanceByCategory(anyLong());
    }

    @Test
    void getEventStats_WhenAuthorized_ReturnsStats() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        when(eventSecurityService.isOwner(eventId, mockUser)).thenReturn(true);

        // Mock repository methods for chart data
        List<ZoneFillRateDataPointDto> zoneFillRates = new ArrayList<>();
        zoneFillRates.add(new ZoneFillRateDataPointDto("Zone 1", 50, 100));
        when(statisticsRepository.findZoneFillRatesByEventId(eventId)).thenReturn(zoneFillRates);

        List<Map<String, Object>> reservationsByDay = new ArrayList<>();
        Map<String, Object> day = new HashMap<>();
        day.put("date", "2023-01-01");
        day.put("count", 10L); // Use Long for counts
        reservationsByDay.add(day);
        when(statisticsRepository.findReservationsByDay(eventId)).thenReturn(reservationsByDay);

        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        Map<String, Object> status = new HashMap<>();
        status.put("status", "VALID");
        status.put("count", 80L); // Use Long for counts
        statusDistribution.add(status);
        when(statisticsRepository.findTicketStatusDistribution(eventId)).thenReturn(statusDistribution);

        // Act
        EventStatisticsDto result = statisticsService.getEventStats(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals(mockEvent.getName(), result.getEventName());
        assertNotNull(result.getZoneFillRateChart());
        assertNotNull(result.getReservationsOverTimeChart());
        assertNotNull(result.getTicketStatusChart());

        // Verify repository methods were called
        verify(statisticsRepository).findZoneFillRatesByEventId(eventId);
        verify(statisticsRepository).findReservationsByDay(eventId);
        verify(statisticsRepository).findTicketStatusDistribution(eventId);
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
    void getEventStats_WhenUnauthorized_ThrowsAccessDeniedException() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        when(eventSecurityService.isOwner(eventId, mockUser)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> statisticsService.getEventStats(eventId));

        // Verify repository methods were not called
        verify(statisticsRepository, never()).findZoneFillRatesByEventId(anyLong());
        verify(statisticsRepository, never()).findReservationsByDay(anyLong());
        verify(statisticsRepository, never()).findTicketStatusDistribution(anyLong());
    }

    @Test
    void getStructureDashboardStats_WhenNoEvents_ReturnsEmptyCharts() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(structureSecurityService.isStructureStaff(eq(structureId), any(Authentication.class))).thenReturn(true);

        // Mock repository methods to return empty results
        when(eventRepository.countByStructureIdAndStartDateAfterAndDeletedFalse(eq(structureId), any(Instant.class))).thenReturn(0L);
        when(ticketRepository.countByEventStructureIdAndStatusIn(eq(structureId), any(Collection.class))).thenReturn(0L);
        when(ticketRepository.countByEventStructureIdAndEventStartDateAfterAndStatus(eq(structureId), any(Instant.class), any(TicketStatus.class))).thenReturn(0L);
        when(eventRepository.findByStructureIdAndEndDateBeforeAndDeletedFalse(eq(structureId), any(Instant.class))).thenReturn(Collections.emptyList());
        when(statisticsRepository.findTopEventsByTickets(eq(structureId), anyInt())).thenReturn(Collections.emptyList());
        when(statisticsRepository.findAttendanceByCategory(structureId)).thenReturn(Collections.emptyList());

        // Act
        StructureDashboardStatsDto result = statisticsService.getStructureDashboardStats(structureId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getUpcomingEventsCount());
        assertEquals(0, result.getTotalTicketsReserved());
        assertEquals(0, result.getTotalExpectedAttendees());
        assertEquals(0.0, result.getAverageAttendanceRate());

        // Charts should be empty but not null
        assertNotNull(result.getTopEventsChart());
        assertNotNull(result.getAttendanceByCategoryChart());
        assertTrue(result.getTopEventsChart().getLabels().isEmpty());
        assertTrue(result.getAttendanceByCategoryChart().getLabels().isEmpty());
    }

    @Test
    void getStructureDashboardStats_WhenNoTickets_ReturnsZeroValues() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(structureSecurityService.isStructureStaff(eq(structureId), any(Authentication.class))).thenReturn(true);

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
    void getEventStats_WhenNoZones_ReturnsEmptyCharts() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        when(eventSecurityService.isOwner(eventId, mockUser)).thenReturn(true);

        // Mock repository methods to return empty results
        when(statisticsRepository.findZoneFillRatesByEventId(eventId)).thenReturn(Collections.emptyList());
        when(statisticsRepository.findReservationsByDay(eventId)).thenReturn(Collections.emptyList());
        when(statisticsRepository.findTicketStatusDistribution(eventId)).thenReturn(Collections.emptyList());

        // Act
        EventStatisticsDto result = statisticsService.getEventStats(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals(mockEvent.getName(), result.getEventName());

        // Charts should be empty but not null
        assertNotNull(result.getZoneFillRateChart());
        assertNotNull(result.getReservationsOverTimeChart());
        assertNotNull(result.getTicketStatusChart());
        assertTrue(result.getZoneFillRateChart().getLabels().isEmpty());
        assertTrue(result.getReservationsOverTimeChart().getLabels().isEmpty());
        assertTrue(result.getTicketStatusChart().getLabels().isEmpty());
    }

    @Test
    void calculateAverageAttendanceRate_WhenPastEventsWithTickets_ReturnsCorrectRate() {
        // Arrange
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(structureSecurityService.isStructureStaff(eq(structureId), any(Authentication.class))).thenReturn(true);

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
}
