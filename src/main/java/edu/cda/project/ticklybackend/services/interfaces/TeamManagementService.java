package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;
import edu.cda.project.ticklybackend.models.user.User;

import java.util.List;

public interface TeamManagementService {
    List<TeamMemberDto> getTeamMembers(Long structureId);

    void inviteMember(Long structureId, InviteMemberRequestDto inviteDto);

    void acceptInvitation(String token, User acceptingUser);

    TeamMemberDto updateMemberRole(Long memberId, UpdateMemberRoleDto roleDto);

    void removeMember(Long memberId);

    long countAdminsForStructure(Long structureId);
}