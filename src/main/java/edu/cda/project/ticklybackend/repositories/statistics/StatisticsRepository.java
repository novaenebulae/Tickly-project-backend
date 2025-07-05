package edu.cda.project.ticklybackend.repositories.statistics;

import edu.cda.project.ticklybackend.dtos.statistics.ZoneFillRateDataPointDto;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository for statistics-related queries.
 * Contains complex, read-only aggregation queries for generating statistics.
 */
@Repository
public interface StatisticsRepository {

    /**
     * Finds the fill rate for each zone in an event.
     *
     * @param eventId The ID of the event
     * @return A list of ZoneFillRateDataPointDto objects
     */
    List<ZoneFillRateDataPointDto> findZoneFillRatesByEventId(@Param("eventId") Long eventId);

    /**
     * Finds the number of reservations by day for an event.
     *
     * @param eventId The ID of the event
     * @return A list of objects with date and count properties
     */
    List<Map<String, Object>> findReservationsByDay(@Param("eventId") Long eventId);

    /**
     * Finds the distribution of ticket statuses for an event.
     *
     * @param eventId The ID of the event
     * @return A list of objects with status and count properties
     */
    List<Map<String, Object>> findTicketStatusDistribution(@Param("eventId") Long eventId);

    /**
     * Finds the top events by ticket count for a structure.
     *
     * @param structureId The ID of the structure
     * @param limit The maximum number of events to return
     * @return A list of objects with name and ticket_count properties
     */
    List<Map<String, Object>> findTopEventsByTickets(@Param("structureId") Long structureId, @Param("limit") int limit);

    /**
     * Finds the attendance by category for a structure.
     *
     * @param structureId The ID of the structure
     * @return A list of objects with name and attendee_count properties
     */
    List<Map<String, Object>> findAttendanceByCategory(@Param("structureId") Long structureId);

    /**
     * Counts the number of unique reservations for an event.
     *
     * @param eventId The ID of the event
     * @return The count of unique reservations
     */
    long countUniqueReservationsByEventId(@Param("eventId") Long eventId);
}
