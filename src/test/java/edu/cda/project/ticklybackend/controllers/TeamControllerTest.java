package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.team.InvitationAcceptanceResponseDto;
import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;
import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TeamControllerTest {

    @Mock
    private TeamManagementService teamService;

    @InjectMocks
    private TeamController teamController;

    private Long structureId;
    private Long memberId;
    private TeamMemberDto teamMemberDto;
    private List<TeamMemberDto> teamMemberDtoList;
    private InviteMemberRequestDto inviteMemberRequestDto;
    private UpdateMemberRoleDto updateMemberRoleDto;
    private InvitationAcceptanceResponseDto invitationAcceptanceResponseDto;
    private String invitationToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up common test data
        structureId = 1L;
        memberId = 1L;
        invitationToken = "valid-invitation-token";

        // Create team member DTO
        teamMemberDto = new TeamMemberDto();
        teamMemberDto.setId(memberId);
        teamMemberDto.setUserId(1L);
        teamMemberDto.setFirstName("Test");
        teamMemberDto.setLastName("User");
        teamMemberDto.setEmail("test@example.com");
        teamMemberDto.setAvatarUrl("http://example.com/avatar.jpg");
        teamMemberDto.setRole(UserRole.ORGANIZATION_SERVICE);
        teamMemberDto.setStatus(TeamMemberStatus.ACTIVE);
        teamMemberDto.setJoinedAt(ZonedDateTime.now());

        // Create team member DTO list
        teamMemberDtoList = new ArrayList<>();
        teamMemberDtoList.add(teamMemberDto);

        // Create invite member request DTO
        inviteMemberRequestDto = new InviteMemberRequestDto();
        inviteMemberRequestDto.setEmail("new.member@example.com");
        inviteMemberRequestDto.setRole(UserRole.ORGANIZATION_SERVICE);

        // Create update member role DTO
        updateMemberRoleDto = new UpdateMemberRoleDto();
        updateMemberRoleDto.setRole(UserRole.RESERVATION_SERVICE);

        // Create invitation acceptance response DTO
        invitationAcceptanceResponseDto = new InvitationAcceptanceResponseDto(
                "jwt-token",
                3600000L,
                structureId,
                "Test Structure",
                "Invitation accepted successfully"
        );
    }

    @Test
    void getTeamMembers_ShouldReturnTeamMembersList() {
        // Arrange
        when(teamService.getTeamMembers(structureId)).thenReturn(teamMemberDtoList);

        // Act
        ResponseEntity<List<TeamMemberDto>> response = teamController.getTeamMembers(structureId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(memberId, response.getBody().get(0).getId());
        assertEquals("test@example.com", response.getBody().get(0).getEmail());
        assertEquals(UserRole.ORGANIZATION_SERVICE, response.getBody().get(0).getRole());

        // Verify
        verify(teamService, times(1)).getTeamMembers(structureId);
    }

    @Test
    void inviteMember_ShouldReturnUpdatedTeamMembersList() {
        // Arrange
        doNothing().when(teamService).inviteMember(eq(structureId), any(InviteMemberRequestDto.class));
        when(teamService.getTeamMembers(structureId)).thenReturn(teamMemberDtoList);

        // Act
        ResponseEntity<List<TeamMemberDto>> response = teamController.inviteMember(structureId, inviteMemberRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // Verify
        verify(teamService, times(1)).inviteMember(structureId, inviteMemberRequestDto);
        verify(teamService, times(1)).getTeamMembers(structureId);
    }

    @Test
    void acceptInvitation_ShouldReturnInvitationAcceptanceResponse() {
        // Arrange
        when(teamService.acceptInvitation(invitationToken)).thenReturn(invitationAcceptanceResponseDto);

        // Act
        ResponseEntity<InvitationAcceptanceResponseDto> response = teamController.acceptInvitation(invitationToken);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getAccessToken());
        assertEquals(3600000L, response.getBody().getExpiresIn());
        assertEquals(structureId, response.getBody().getStructureId());
        assertEquals("Test Structure", response.getBody().getStructureName());
        assertEquals("Invitation accepted successfully", response.getBody().getMessage());

        // Verify
        verify(teamService, times(1)).acceptInvitation(invitationToken);
    }

    @Test
    void updateMemberRole_ShouldReturnUpdatedTeamMember() {
        // Arrange
        when(teamService.updateMemberRole(eq(memberId), any(UpdateMemberRoleDto.class))).thenReturn(teamMemberDto);

        // Act
        ResponseEntity<TeamMemberDto> response = teamController.updateMemberRole(memberId, updateMemberRoleDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(memberId, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals(UserRole.ORGANIZATION_SERVICE, response.getBody().getRole());

        // Verify
        verify(teamService, times(1)).updateMemberRole(memberId, updateMemberRoleDto);
    }

    @Test
    void removeMember_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(teamService).removeMember(memberId);

        // Act
        ResponseEntity<Void> response = teamController.removeMember(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(teamService, times(1)).removeMember(memberId);
    }
}