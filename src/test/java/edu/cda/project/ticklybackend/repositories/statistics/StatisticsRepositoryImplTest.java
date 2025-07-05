package edu.cda.project.ticklybackend.repositories.statistics;

import edu.cda.project.ticklybackend.dtos.statistics.ZoneFillRateDataPointDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private StatisticsRepositoryImpl statisticsRepository;

    private Long eventId = 1L;
    private Long structureId = 1L;

    @BeforeEach
    void setUp() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    }

    @Test
    void findZoneFillRatesByEventId_ReturnsCorrectData() {
        // Arrange
        Object[] row1 = new Object[]{"Zone 1", 100, 50L};
        Object[] row2 = new Object[]{"Zone 2", 200, 150L};
        List<Object[]> mockResults = Arrays.asList(row1, row2);
        
        when(query.setParameter(eq("eventId"), eq(eventId))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        List<ZoneFillRateDataPointDto> result = statisticsRepository.findZoneFillRatesByEventId(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("Zone 1", result.get(0).getZoneName());
        assertEquals(100, result.get(0).getCapacity());
        assertEquals(50L, result.get(0).getTicketsSold());
        
        assertEquals("Zone 2", result.get(1).getZoneName());
        assertEquals(200, result.get(1).getCapacity());
        assertEquals(150L, result.get(1).getTicketsSold());
        
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("eventId", eventId);
        verify(query).getResultList();
    }

    @Test
    void findReservationsByDay_ReturnsCorrectData() {
        // Arrange
        Object[] row1 = new Object[]{"2023-01-01", 10L};
        Object[] row2 = new Object[]{"2023-01-02", 20L};
        List<Object[]> mockResults = Arrays.asList(row1, row2);
        
        when(query.setParameter(eq("eventId"), eq(eventId))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        List<Map<String, Object>> result = statisticsRepository.findReservationsByDay(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("2023-01-01", result.get(0).get("date"));
        assertEquals(10L, result.get(0).get("count"));
        
        assertEquals("2023-01-02", result.get(1).get("date"));
        assertEquals(20L, result.get(1).get("count"));
        
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("eventId", eventId);
        verify(query).getResultList();
    }

    @Test
    void findTicketStatusDistribution_ReturnsCorrectData() {
        // Arrange
        Object[] row1 = new Object[]{"VALID", 80L};
        Object[] row2 = new Object[]{"USED", 20L};
        List<Object[]> mockResults = Arrays.asList(row1, row2);
        
        when(query.setParameter(eq("eventId"), eq(eventId))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        List<Map<String, Object>> result = statisticsRepository.findTicketStatusDistribution(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("VALID", result.get(0).get("status"));
        assertEquals(80L, result.get(0).get("count"));
        
        assertEquals("USED", result.get(1).get("status"));
        assertEquals(20L, result.get(1).get("count"));
        
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("eventId", eventId);
        verify(query).getResultList();
    }

    @Test
    void findTopEventsByTickets_ReturnsCorrectData() {
        // Arrange
        Object[] row1 = new Object[]{"Event 1", 100L};
        Object[] row2 = new Object[]{"Event 2", 50L};
        List<Object[]> mockResults = Arrays.asList(row1, row2);
        
        when(query.setParameter(eq("structureId"), eq(structureId))).thenReturn(query);
        when(query.setParameter(eq("limit"), anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        List<Map<String, Object>> result = statisticsRepository.findTopEventsByTickets(structureId, 5);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("Event 1", result.get(0).get("name"));
        assertEquals(100L, result.get(0).get("ticket_count"));
        
        assertEquals("Event 2", result.get(1).get("name"));
        assertEquals(50L, result.get(1).get("ticket_count"));
        
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("structureId", structureId);
        verify(query).setParameter("limit", 5);
        verify(query).getResultList();
    }

    @Test
    void findAttendanceByCategory_ReturnsCorrectData() {
        // Arrange
        Object[] row1 = new Object[]{"Category 1", 50L};
        Object[] row2 = new Object[]{"Category 2", 30L};
        List<Object[]> mockResults = Arrays.asList(row1, row2);
        
        when(query.setParameter(eq("structureId"), eq(structureId))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        List<Map<String, Object>> result = statisticsRepository.findAttendanceByCategory(structureId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("Category 1", result.get(0).get("name"));
        assertEquals(50L, result.get(0).get("attendee_count"));
        
        assertEquals("Category 2", result.get(1).get("name"));
        assertEquals(30L, result.get(1).get("attendee_count"));
        
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("structureId", structureId);
        verify(query).getResultList();
    }
}