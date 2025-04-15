package edu.cda.project.ticklybackend.daos.eventDao;

import edu.cda.project.ticklybackend.models.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDao extends JpaRepository<Event, Integer> {

}
