package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@Tag(name = "Gestion d'Équipe", description = "API pour la gestion des membres de l'équipe d'une structure.")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamManagementService teamService;

    @GetMapping("/structure/{structureId}")
    @Operation(summary = "Récupérer les membres de l'équipe d'une structure")
    @PreAuthorize("@teamSecurityService.isStructureAdmin(#structureId, authentication.principal)")
    public ResponseEntity<List<TeamMemberDto>> getTeamMembers(@PathVariable Long structureId) {
        return ResponseEntity.ok(teamService.getTeamMembers(structureId));
    }

    @PostMapping("/structure/{structureId}/invite")
    @Operation(summary = "Inviter un nouveau membre dans l'équipe")
    @PreAuthorize("@teamSecurityService.isStructureAdmin(#structureId, authentication.principal)")
    public ResponseEntity<Void> inviteMember(@PathVariable Long structureId, @Valid @RequestBody InviteMemberRequestDto inviteDto) {
        teamService.inviteMember(structureId, inviteDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/invitations/accept")
    @Operation(summary = "Accepter une invitation à rejoindre une équipe", description = "L'utilisateur doit être authentifié. Le token est passé en paramètre de requête.")
    @PreAuthorize("isAuthenticated()") // Seul un utilisateur connecté peut accepter une invitation
    public ResponseEntity<Void> acceptInvitation(@RequestParam String token, @AuthenticationPrincipal User currentUser) {
        teamService.acceptInvitation(token, currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/members/{memberId}/role")
    @Operation(summary = "Mettre à jour le rôle d'un membre de l'équipe")
    @PreAuthorize("@teamSecurityService.isTeamAdminOfMember(#memberId, authentication.principal)")
    public ResponseEntity<TeamMemberDto> updateMemberRole(@PathVariable Long memberId, @Valid @RequestBody UpdateMemberRoleDto roleDto) {
        return ResponseEntity.ok(teamService.updateMemberRole(memberId, roleDto));
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "Supprimer un membre de l'équipe")
    @PreAuthorize("@teamSecurityService.isTeamAdminOfMember(#memberId, authentication.principal)")
    public ResponseEntity<Void> removeMember(@PathVariable Long memberId) {
        teamService.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }
}