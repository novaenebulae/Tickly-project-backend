package edu.cda.project.ticklybackend.repositories.team;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamStructureId(Long structureId);

    boolean existsByTeamIdAndEmail(Long teamId, String email);

    long countByTeamStructureIdAndRole(Long structureId, UserRole role);
}