package edu.cda.project.ticklybackend.repositories.event;

import edu.cda.project.ticklybackend.models.event.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {
}