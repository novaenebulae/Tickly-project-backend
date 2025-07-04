package edu.cda.project.ticklybackend.repositories.team;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamStructureId(Long structureId);

    boolean existsByTeamIdAndEmail(Long teamId, String email);

    long countByTeamStructureIdAndRole(Long structureId, UserRole role);

    long deleteByTeamId(Long id);

    Optional<TeamMember> findByUserId(Long id);
}