package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.team.InvitationAcceptanceResponseDto;
import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints to manage structure team members: list, invite, accept invitation,
 * update roles, and remove members.
 */
@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "API for managing team members of a structure.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TeamController {

    private final TeamManagementService teamService;

    @GetMapping("/structure/{structureId}")
    @Operation(summary = "Get team members of a structure")
    @PreAuthorize("@organizationalSecurityService.canAccessStructure(#structureId, authentication)")
    public ResponseEntity<List<TeamMemberDto>> getTeamMembers(@PathVariable Long structureId) {
        log.info("Retrieving team members for structure ID: {}", structureId);
        try {
            List<TeamMemberDto> members = teamService.getTeamMembers(structureId);
            log.info("Successfully retrieved {} members for structure ID: {}", members.size(), structureId);
            return ResponseEntity.ok(members);
        } catch (ResourceNotFoundException e) {
            log.error("Error retrieving team members for structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while retrieving team members for structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/structure/{structureId}/invite")
    @Operation(summary = "Invite a new member to the team")
    @PreAuthorize("@organizationalSecurityService.isStructureAdmin(#structureId, authentication)")
    public ResponseEntity<List<TeamMemberDto>> inviteMember(@PathVariable Long structureId, @Valid @RequestBody InviteMemberRequestDto inviteDto) {
        log.info("Inviting a new team member for structure ID: {}, email: {}", structureId, inviteDto.getEmail());
        try {
            teamService.inviteMember(structureId, inviteDto);
            log.info("Invitation successfully sent to {} for structure ID: {}", inviteDto.getEmail(), structureId);
            List<TeamMemberDto> updatedMembers = teamService.getTeamMembers(structureId);
            return ResponseEntity.ok(updatedMembers);
        } catch (ResourceNotFoundException e) {
            log.error("Error inviting a member for structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Invalid request when inviting a member for structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while inviting a member for structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/invitations/accept")
    @Operation(
            summary = "Accept an invitation to join a team",
            description = "Public endpoint to accept a team invitation. The invitation token is passed as a parameter. " +
                    "The user is automatically identified via the invitation token. " +
                    "Returns a new JWT token with updated permissions.",
            security = {}
    )
    public ResponseEntity<InvitationAcceptanceResponseDto> acceptInvitation(@RequestParam String token) {
        log.info("Attempting to accept team invitation with token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        try {
            InvitationAcceptanceResponseDto response = teamService.acceptInvitation(token);
            log.info("Invitation successfully accepted for structure ID: {}, name: {}", response.getStructureId(), response.getStructureName());
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            log.warn("Invalid request when accepting invitation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error accepting invitation: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/members/{memberId}/role")
    @Operation(summary = "Update a team member's role")
    @PreAuthorize("@organizationalSecurityService.canManageTeamMember(#memberId, authentication)")
    public ResponseEntity<TeamMemberDto> updateMemberRole(@PathVariable Long memberId, @Valid @RequestBody UpdateMemberRoleDto roleDto) {
        log.info("Updating role of member ID: {} to role: {}", memberId, roleDto.getRole());
        try {
            TeamMemberDto updatedMember = teamService.updateMemberRole(memberId, roleDto);
            log.info("Member ID: {} role successfully updated to: {}", memberId, roleDto.getRole());
            return ResponseEntity.ok(updatedMember);
        } catch (ResourceNotFoundException e) {
            log.error("Team member not found - ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Invalid request when updating role of member ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating role of member ID: {}: {}", memberId, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "Remove a team member")
    @PreAuthorize("@organizationalSecurityService.canManageTeamMember(#memberId, authentication)")
    public ResponseEntity<Void> removeMember(@PathVariable Long memberId) {
        log.info("Removing team member ID: {}", memberId);
        try {
            teamService.removeMember(memberId);
            log.info("Team member ID: {} successfully removed", memberId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Team member not found - ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Invalid request when removing member ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while removing member ID: {}: {}", memberId, e.getMessage());
            throw e;
        }
    }
}
