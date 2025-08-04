package edu.cda.project.ticklybackend.repositories.event;

import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.models.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    // Override standard findById to exclude deleted events
    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.deleted = false")
    Optional<Event> findById(@Param("id") Long id);

    // Find by ID including deleted events (for admin purposes)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdIncludingDeleted(@Param("id") Long id);

    // Find all non-deleted events
    @Query("SELECT e FROM Event e WHERE e.deleted = false")
    List<Event> findAll();

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.audienceZones az " +
            "LEFT JOIN FETCH az.template t " +
            "LEFT JOIN FETCH t.area a " +
            "LEFT JOIN FETCH e.categories " +
            "LEFT JOIN FETCH e.structure " +
            "WHERE e.id = :id AND e.deleted = false")
    Optional<Event> findByIdWithAudienceZones(@Param("id") Long id);

    boolean existsByStructureIdAndStatusIn(Long structureId, Set<EventStatus> published);

    @Query("SELECT COUNT(e) > 0 FROM Event e " +
            "JOIN e.audienceZones az " +
            "JOIN az.template t " +
            "WHERE t.area.id = :areaId AND e.status IN :statuses")
    boolean existsByAreaIdAndStatusIn(@Param("areaId") Long areaId, @Param("statuses") Set<EventStatus> statuses);

    @Query("SELECT COUNT(e) > 0 FROM Event e " +
            "JOIN e.audienceZones az " +
            "WHERE az.template.id = :templateId AND e.status IN :statuses")
    boolean existsByTemplateIdAndStatusIn(@Param("templateId") Long templateId, @Param("statuses") Set<EventStatus> statuses);

    /**
     * Counts upcoming events for a structure.
     *
     * @param structureId The ID of the structure
     * @param startDate   The date after which events are considered upcoming
     * @return The count of upcoming events
     */
    long countByStructureIdAndStartDateAfterAndDeletedFalse(Long structureId, Instant startDate);

    /**
     * Finds past events for a structure.
     *
     * @param structureId The ID of the structure
     * @param endDate     The date before which events are considered past
     * @return A list of past events
     */
    List<Event> findByStructureIdAndEndDateBeforeAndDeletedFalse(Long structureId, Instant endDate);

    /**
     * Checks if there are any conflicting events in the same areas and time slots.
     * A conflict occurs when an event is scheduled in the same area with overlapping time slots.
     *
     * @param structureId    The ID of the structure
     * @param startDate      The start date of the event to check
     * @param endDate        The end date of the event to check
     * @param areaIds        The IDs of the areas to check
     * @param excludeEventId Optional event ID to exclude from the check (for updates)
     * @return A list of conflicting events, empty if no conflicts
     */
    @Query("SELECT e FROM Event e " +
            "JOIN e.audienceZones az " +
            "JOIN az.template t " +
            "WHERE e.structure.id = :structureId " +
            "AND e.deleted = false " +
            "AND e.status != 'CANCELLED' " +
            "AND t.area.id IN :areaIds " +
            "AND (:excludeEventId IS NULL OR e.id != :excludeEventId) " +
            "AND NOT (e.endDate <= :startDate OR e.startDate >= :endDate)")
    List<Event> findConflictingEvents(
            @Param("structureId") Long structureId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("areaIds") Set<Long> areaIds,
            @Param("excludeEventId") Long excludeEventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Event e SET e.status = :completedStatus WHERE e.id = :eventId AND e.status = :publishedStatus AND e.endDate < CURRENT_TIMESTAMP")
    int updateEventStatusToCompleted(
            @Param("eventId") Long eventId,
            @Param("completedStatus") EventStatus completedStatus,
            @Param("publishedStatus") EventStatus publishedStatus
    );
}
