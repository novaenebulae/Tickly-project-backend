package edu.cda.project.ticklybackend.repositories.team;

import edu.cda.project.ticklybackend.models.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByStructureId(Long structureId);
}