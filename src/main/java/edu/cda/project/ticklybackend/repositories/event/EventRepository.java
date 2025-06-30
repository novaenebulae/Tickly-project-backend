package edu.cda.project.ticklybackend.repositories.event;

import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.models.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.audienceZones az " +
            "LEFT JOIN FETCH az.template t " +
            "LEFT JOIN FETCH t.area a " +
            "LEFT JOIN FETCH e.categories " +
            "LEFT JOIN FETCH e.structure " +
            "WHERE e.id = :id")
    Optional<Event> findByIdWithAudienceZones(@Param("id") Long id);

    boolean existsByStructureIdAndStatusIn(Long structureId, Set<EventStatus> published);
}