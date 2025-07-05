package edu.cda.project.ticklybackend.repositories.statistics;

import edu.cda.project.ticklybackend.dtos.statistics.ZoneFillRateDataPointDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the StatisticsRepository interface.
 * Uses EntityManager to execute native SQL queries.
 */
@Repository
public class StatisticsRepositoryImpl implements StatisticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ZoneFillRateDataPointDto> findZoneFillRatesByEventId(Long eventId) {
        String sql = "SELECT azt.name as zoneName, eaz.allocated_capacity as capacity, COUNT(t.id) as ticketsSold " +
                "FROM event_audience_zone eaz " +
                "JOIN audience_zone_template azt ON eaz.template_id = azt.id " +
                "LEFT JOIN tickets t ON eaz.id = t.event_audience_zone_id AND t.status IN ('VALID', 'USED') " +
                "WHERE eaz.event_id = :eventId " +
                "GROUP BY eaz.id, azt.name, eaz.allocated_capacity";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("eventId", eventId);

        List<Object[]> results = query.getResultList();
        List<ZoneFillRateDataPointDto> dtos = new ArrayList<>();

        for (Object[] result : results) {
            String zoneName = (String) result[0];
            int capacity = ((Number) result[1]).intValue();
            long ticketsSold = ((Number) result[2]).longValue();

            dtos.add(new ZoneFillRateDataPointDto(zoneName, ticketsSold, capacity));
        }

        return dtos;
    }

    @Override
    public List<Map<String, Object>> findReservationsByDay(Long eventId) {
        String sql = "SELECT DATE(reservation_date) as date, COUNT(*) as count " +
                "FROM tickets " +
                "WHERE event_id = :eventId AND status IN ('VALID', 'USED') " +
                "GROUP BY DATE(reservation_date) " +
                "ORDER BY date ASC";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("eventId", eventId);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", result[0]);
            map.put("count", result[1]);
            resultList.add(map);
        }

        return resultList;
    }

    @Override
    public List<Map<String, Object>> findTicketStatusDistribution(Long eventId) {
        String sql = "SELECT status, COUNT(*) as count " +
                "FROM tickets " +
                "WHERE event_id = :eventId " +
                "GROUP BY status";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("eventId", eventId);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", result[0]);
            map.put("count", result[1]);
            resultList.add(map);
        }

        return resultList;
    }

    @Override
    public List<Map<String, Object>> findTopEventsByTickets(Long structureId, int limit) {
        String sql = "SELECT e.name, COUNT(t.id) as ticket_count " +
                "FROM events e JOIN tickets t ON e.id = t.event_id " +
                "WHERE e.structure_id = :structureId AND t.status IN ('VALID', 'USED') " +
                "GROUP BY e.id, e.name " +
                "ORDER BY ticket_count DESC " +
                "LIMIT :limit";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("structureId", structureId);
        query.setParameter("limit", limit);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", result[0]);
            map.put("ticket_count", result[1]);
            resultList.add(map);
        }

        return resultList;
    }

    @Override
    public List<Map<String, Object>> findAttendanceByCategory(Long structureId) {
        String sql = "SELECT ec.name, COUNT(t.id) as attendee_count " +
                "FROM tickets t " +
                "JOIN event_has_categories ehc ON t.event_id = ehc.event_id " +
                "JOIN event_categories ec ON ehc.category_id = ec.id " +
                "JOIN events e ON t.event_id = e.id " +
                "WHERE e.structure_id = :structureId AND t.status IN ('VALID', 'USED') " +
                "GROUP BY ec.id, ec.name " +
                "ORDER BY attendee_count DESC";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("structureId", structureId);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", result[0]);
            map.put("attendee_count", result[1]);
            resultList.add(map);
        }

        return resultList;
    }

    @Override
    public long countUniqueReservationsByEventId(Long eventId) {
        String sql = "SELECT COUNT(DISTINCT reservation_id) " +
                "FROM tickets " +
                "WHERE event_id = :eventId";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("eventId", eventId);

        Number result = (Number) query.getSingleResult();
        return result.longValue();
    }
}
