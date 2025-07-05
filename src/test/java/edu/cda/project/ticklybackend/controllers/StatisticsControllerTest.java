package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.statistics.ChartJsDataDto;
import edu.cda.project.ticklybackend.dtos.statistics.EventStatisticsDto;
import edu.cda.project.ticklybackend.dtos.statistics.StructureDashboardStatsDto;
import edu.cda.project.ticklybackend.exceptions.AccessDeniedException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    private StructureDashboardStatsDto structureDashboardStatsDto;
    private EventStatisticsDto eventStatisticsDto;

    @BeforeEach
    void setUp() {
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
    @WithMockUser(roles = "STRUCTURE_ADMINISTRATOR")
    void getStructureDashboardStats_WhenAuthorized_ReturnsStats() throws Exception {
        // Arrange
        when(statisticsService.getStructureDashboardStats(anyLong())).thenReturn(structureDashboardStatsDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/statistics/structure/1/dashboard")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingEventsCount").value(10))
                .andExpect(jsonPath("$.totalTicketsReserved").value(100))
                .andExpect(jsonPath("$.totalExpectedAttendees").value(75))
                .andExpect(jsonPath("$.averageAttendanceRate").value(85.5))
                .andExpect(jsonPath("$.topEventsChart").exists())
                .andExpect(jsonPath("$.attendanceByCategoryChart").exists());
    }

    @Test
    @WithMockUser(roles = "STRUCTURE_ADMINISTRATOR")
    void getStructureDashboardStats_WhenUnauthorized_ReturnsForbidden() throws Exception {
        // Arrange
        when(statisticsService.getStructureDashboardStats(anyLong())).thenThrow(new AccessDeniedException("Access denied"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/statistics/structure/1/dashboard")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STRUCTURE_ADMINISTRATOR")
    void getStructureDashboardStats_WhenStructureNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(statisticsService.getStructureDashboardStats(anyLong())).thenThrow(new ResourceNotFoundException("Structure", "id", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/statistics/structure/1/dashboard")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STRUCTURE_ADMINISTRATOR")
    void getEventStats_WhenAuthorized_ReturnsStats() throws Exception {
        // Arrange
        when(statisticsService.getEventStats(anyLong())).thenReturn(eventStatisticsDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/statistics/event/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.eventName").value("Test Event"))
                .andExpect(jsonPath("$.zoneFillRateChart").exists())
                .andExpect(jsonPath("$.reservationsOverTimeChart").exists())
                .andExpect(jsonPath("$.ticketStatusChart").exists());
    }

    @Test
    @WithMockUser(roles = "STRUCTURE_ADMINISTRATOR")
    void getEventStats_WhenUnauthorized_ReturnsForbidden() throws Exception {
        // Arrange
        when(statisticsService.getEventStats(anyLong())).thenThrow(new AccessDeniedException("Access denied"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/statistics/event/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STRUCTURE_ADMINISTRATOR")
    void getEventStats_WhenEventNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(statisticsService.getEventStats(anyLong())).thenThrow(new ResourceNotFoundException("Event", "id", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/statistics/event/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}