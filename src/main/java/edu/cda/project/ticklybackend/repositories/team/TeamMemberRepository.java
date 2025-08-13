package edu.cda.project.ticklybackend.repositories.team;

import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByStructureId(Long structureId);

    boolean existsByStructureIdAndEmail(Long structureId, String email);

    long countByStructureIdAndRole(Long structureId, UserRole role);

    long deleteByStructureId(Long structureId);

    Optional<TeamMember> findByUserId(Long id);

    // New helpers for membership-based security checks
    Optional<TeamMember> findByUserIdAndStructureId(Long userId, Long structureId);

    boolean existsByUserIdAndStructureId(Long userId, Long structureId);

    List<TeamMember> findByStructureIdAndRole(Long structureId, UserRole role);

    Optional<TeamMember> findFirstByUserIdAndStatusOrderByJoinedAtDesc(Long userId, TeamMemberStatus status);
}