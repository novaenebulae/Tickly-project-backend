package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.statistics.ChartJsDataDto;
import edu.cda.project.ticklybackend.dtos.statistics.EventStatisticsDto;
import edu.cda.project.ticklybackend.dtos.statistics.StructureDashboardStatsDto;
import edu.cda.project.ticklybackend.exceptions.AccessDeniedException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    private StructureDashboardStatsDto structureDashboardStatsDto;
    private EventStatisticsDto eventStatisticsDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock StructureDashboardStatsDto
        ChartJsDataDto topEventsChart = new ChartJsDataDto("bar", Collections.singletonList("Event 1"), Collections.emptyList());
        ChartJsDataDto attendanceByCategoryChart = new ChartJsDataDto("doughnut", Collections.singletonList("Category 1"), Collections.emptyList());

        structureDashboardStatsDto = new StructureDashboardStatsDto(
                10L,
                100L,
                75L,
                85.5,
                topEventsChart,
                attendanceByCategoryChart
        );

        // Setup mock EventStatisticsDto
        ChartJsDataDto zoneFillRateChart = new ChartJsDataDto("bar", Collections.singletonList("Zone 1"), Collections.emptyList());
        ChartJsDataDto reservationsOverTimeChart = new ChartJsDataDto("line", Collections.singletonList("2023-01-01"), Collections.emptyList());
        ChartJsDataDto ticketStatusChart = new ChartJsDataDto("doughnut", Collections.singletonList("VALID"), Collections.emptyList());

        eventStatisticsDto = new EventStatisticsDto(
                1L,
                "Test Event",
                zoneFillRateChart,
                reservationsOverTimeChart,
                ticketStatusChart
        );
    }

    @Test
    void getStructureDashboardStats_WhenAuthorized_ReturnsStats() {
        // Arrange
        Long structureId = 1L;
        when(statisticsService.getStructureDashboardStats(structureId)).thenReturn(structureDashboardStatsDto);

        // Act
        ResponseEntity<StructureDashboardStatsDto> response = statisticsController.getStructureDashboardStats(structureId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getUpcomingEventsCount());
        assertEquals(100L, response.getBody().getTotalTicketsReserved());
        assertEquals(75L, response.getBody().getTotalExpectedAttendees());
        assertEquals(85.5, response.getBody().getAverageAttendanceRate());
        assertNotNull(response.getBody().getTopEventsChart());
        assertNotNull(response.getBody().getAttendanceByCategoryChart());

        // Verify
        verify(statisticsService, times(1)).getStructureDashboardStats(structureId);
    }

    @Test
    void getStructureDashboardStats_WhenUnauthorized_ReturnsForbidden() {
        // Arrange
        Long structureId = 1L;
        doThrow(new AccessDeniedException("Access denied")).when(statisticsService).getStructureDashboardStats(structureId);

        // Act & Assert
        try {
            statisticsController.getStructureDashboardStats(structureId);
        } catch (AccessDeniedException e) {
            assertEquals("Access denied", e.getMessage());
        }

        // Verify
        verify(statisticsService, times(1)).getStructureDashboardStats(structureId);
    }

    @Test
    void getStructureDashboardStats_WhenStructureNotFound_ReturnsNotFound() {
        // Arrange
        Long structureId = 1L;
        doThrow(new ResourceNotFoundException("Structure", "id", structureId)).when(statisticsService).getStructureDashboardStats(structureId);

        // Act & Assert
        try {
            statisticsController.getStructureDashboardStats(structureId);
        } catch (ResourceNotFoundException e) {
            assertEquals("Structure non trouvé(e) avec id : '1'", e.getMessage());
        }

        // Verify
        verify(statisticsService, times(1)).getStructureDashboardStats(structureId);
    }

    @Test
    void getEventStats_WhenAuthorized_ReturnsStats() {
        // Arrange
        Long eventId = 1L;
        when(statisticsService.getEventStats(eventId)).thenReturn(eventStatisticsDto);

        // Act
        ResponseEntity<EventStatisticsDto> response = statisticsController.getEventStats(eventId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getEventId());
        assertEquals("Test Event", response.getBody().getEventName());
        assertNotNull(response.getBody().getZoneFillRateChart());
        assertNotNull(response.getBody().getReservationsOverTimeChart());
        assertNotNull(response.getBody().getTicketStatusChart());

        // Verify
        verify(statisticsService, times(1)).getEventStats(eventId);
    }

    @Test
    void getEventStats_WhenUnauthorized_ReturnsForbidden() {
        // Arrange
        Long eventId = 1L;
        doThrow(new AccessDeniedException("Access denied")).when(statisticsService).getEventStats(eventId);

        // Act & Assert
        try {
            statisticsController.getEventStats(eventId);
        } catch (AccessDeniedException e) {
            assertEquals("Access denied", e.getMessage());
        }

        // Verify
        verify(statisticsService, times(1)).getEventStats(eventId);
    }

    @Test
    void getEventStats_WhenEventNotFound_ReturnsNotFound() {
        // Arrange
        Long eventId = 1L;
        doThrow(new ResourceNotFoundException("Event", "id", eventId)).when(statisticsService).getEventStats(eventId);

        // Act & Assert
        try {
            statisticsController.getEventStats(eventId);
        } catch (ResourceNotFoundException e) {
            assertEquals("Event non trouvé(e) avec id : '1'", e.getMessage());
        }

        // Verify
        verify(statisticsService, times(1)).getEventStats(eventId);
    }
}
