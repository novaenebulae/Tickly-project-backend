package edu.cda.project.ticklybackend.repositories;

import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.models.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    // Trouver les événements par statut (utile pour les listes publiques)
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    // Trouver les événements mis en avant et publiés
    List<Event> findByIsFeaturedEventTrueAndStatus(EventStatus status, Pageable pageable);

    // Trouver les événements à afficher sur la page d'accueil et publiés
    List<Event> findByDisplayOnHomepageTrueAndStatus(EventStatus status, Pageable pageable);

    // Trouver les événements d'une structure spécifique avec un certain statut
    Page<Event> findByStructureIdAndStatus(Long structureId, EventStatus status, Pageable pageable);
}