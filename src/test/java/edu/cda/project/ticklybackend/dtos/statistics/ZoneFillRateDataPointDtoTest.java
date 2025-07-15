package edu.cda.project.ticklybackend.dtos.statistics;

import edu.cda.project.ticklybackend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoneFillRateDataPointDtoTest extends AbstractIntegrationTest {

    @Test
    void getFillRate_WhenCapacityIsZero_ReturnsZero() {
        // Arrange
        ZoneFillRateDataPointDto dto = new ZoneFillRateDataPointDto("Test Zone", 10, 0);

        // Act
        double result = dto.getFillRate();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void getFillRate_WhenTicketsSoldIsZero_ReturnsZero() {
        // Arrange
        ZoneFillRateDataPointDto dto = new ZoneFillRateDataPointDto("Test Zone", 0, 100);

        // Act
        double result = dto.getFillRate();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void getFillRate_WhenTicketsSoldLessThanCapacity_ReturnsCorrectPercentage() {
        // Arrange
        ZoneFillRateDataPointDto dto = new ZoneFillRateDataPointDto("Test Zone", 50, 100);

        // Act
        double result = dto.getFillRate();

        // Assert
        assertEquals(50.0, result);
    }

    @Test
    void getFillRate_WhenTicketsSoldEqualsCapacity_Returns100Percent() {
        // Arrange
        ZoneFillRateDataPointDto dto = new ZoneFillRateDataPointDto("Test Zone", 100, 100);

        // Act
        double result = dto.getFillRate();

        // Assert
        assertEquals(100.0, result);
    }

    @Test
    void getFillRate_WhenTicketsSoldExceedsCapacity_ReturnsMoreThan100Percent() {
        // Arrange
        ZoneFillRateDataPointDto dto = new ZoneFillRateDataPointDto("Test Zone", 150, 100);

        // Act
        double result = dto.getFillRate();

        // Assert
        assertEquals(150.0, result);
    }
}