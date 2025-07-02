package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.team.InvitationAcceptanceResponseDto;
import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;

import java.util.List;

public interface TeamManagementService {
    List<TeamMemberDto> getTeamMembers(Long structureId);

    void inviteMember(Long structureId, InviteMemberRequestDto inviteDto);

    /**
     * Accepte une invitation d'équipe de manière publique (sans authentification préalable).
     * L'utilisateur est identifié via le token d'invitation lui-même.
     *
     * @param token Token d'invitation
     * @return DTO avec le token JWT et les informations de structure
     */
    InvitationAcceptanceResponseDto acceptInvitation(String token);

    TeamMemberDto updateMemberRole(Long memberId, UpdateMemberRoleDto roleDto);

    void removeMember(Long memberId);

    long countAdminsForStructure(Long structureId);

    /**
     * Dissolve a team associated with a structure.
     * This method is called when a structure is deleted.
     * It converts all team members to SPECTATOR role and removes them from the team.
     *
     * @param structureId the ID of the structure whose team should be dissolved
     */
    void dissolveTeam(Long structureId);
}
