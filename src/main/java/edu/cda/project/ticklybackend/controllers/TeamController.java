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

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@Tag(name = "Gestion d'Équipe", description = "API pour la gestion des membres de l'équipe d'une structure.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TeamController {

    private final TeamManagementService teamService;

    @GetMapping("/structure/{structureId}")
    @Operation(summary = "Récupérer les membres de l'équipe d'une structure")
    @PreAuthorize("@teamSecurityService.isStructureAdmin(#structureId, authentication.principal) or @teamSecurityService.isStructureOrganizationService(#structureId, authentication.principal) or @teamSecurityService.isStructureReservationService(#structureId, authentication.principal)")
    public ResponseEntity<List<TeamMemberDto>> getTeamMembers(@PathVariable Long structureId) {
        log.info("Récupération des membres de l'équipe pour la structure ID: {}", structureId);
        try {
            List<TeamMemberDto> members = teamService.getTeamMembers(structureId);
            log.info("Récupération réussie de {} membres pour la structure ID: {}", members.size(), structureId);
            return ResponseEntity.ok(members);
        } catch (ResourceNotFoundException e) {
            log.error("Erreur lors de la récupération des membres de l'équipe pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des membres de l'équipe pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/structure/{structureId}/invite")
    @Operation(summary = "Inviter un nouveau membre dans l'équipe")
    @PreAuthorize("@teamSecurityService.isStructureAdmin(#structureId, authentication.principal)")
    public ResponseEntity<List<TeamMemberDto>> inviteMember(@PathVariable Long structureId, @Valid @RequestBody InviteMemberRequestDto inviteDto) {
        log.info("Invitation d'un nouveau membre dans l'équipe de la structure ID: {}, email: {}", structureId, inviteDto.getEmail());
        try {
            teamService.inviteMember(structureId, inviteDto);
            log.info("Invitation envoyée avec succès à {} pour la structure ID: {}", inviteDto.getEmail(), structureId);
            List<TeamMemberDto> updatedMembers = teamService.getTeamMembers(structureId);
            return ResponseEntity.ok(updatedMembers);
        } catch (ResourceNotFoundException e) {
            log.error("Erreur lors de l'invitation d'un membre pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Requête invalide lors de l'invitation d'un membre pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'invitation d'un membre pour la structure ID: {}: {}", structureId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/invitations/accept")
    @Operation(
            summary = "Accepter une invitation à rejoindre une équipe",
            description = "Endpoint public pour accepter une invitation d'équipe. Le token d'invitation est passé en paramètre. " +
                    "L'utilisateur est automatiquement identifié via le token d'invitation. " +
                    "Retourne un nouveau token JWT avec les permissions mises à jour.",
            security = {} // Supprime l'exigence d'authentification pour cet endpoint
    )
    public ResponseEntity<InvitationAcceptanceResponseDto> acceptInvitation(@RequestParam String token) {
        log.info("Tentative d'acceptation d'invitation d'équipe avec token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        try {
            InvitationAcceptanceResponseDto response = teamService.acceptInvitation(token);
            log.info("Invitation acceptée avec succès pour la structure ID: {}, nom: {}", response.getStructureId(), response.getStructureName());
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            log.warn("Requête invalide lors de l'acceptation d'invitation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de l'acceptation d'invitation: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/members/{memberId}/role")
    @Operation(summary = "Mettre à jour le rôle d'un membre de l'équipe")
    @PreAuthorize("@teamSecurityService.isTeamManager(#memberId, authentication.principal)")
    public ResponseEntity<TeamMemberDto> updateMemberRole(@PathVariable Long memberId, @Valid @RequestBody UpdateMemberRoleDto roleDto) {
        log.info("Mise à jour du rôle du membre ID: {} vers le rôle: {}", memberId, roleDto.getRole());
        try {
            TeamMemberDto updatedMember = teamService.updateMemberRole(memberId, roleDto);
            log.info("Rôle du membre ID: {} mis à jour avec succès vers: {}", memberId, roleDto.getRole());
            return ResponseEntity.ok(updatedMember);
        } catch (ResourceNotFoundException e) {
            log.error("Membre d'équipe non trouvé - ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Requête invalide lors de la mise à jour du rôle du membre ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour du rôle du membre ID: {}: {}", memberId, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "Supprimer un membre de l'équipe")
    @PreAuthorize("@teamSecurityService.isTeamManager(#memberId, authentication.principal)")
    public ResponseEntity<Void> removeMember(@PathVariable Long memberId) {
        log.info("Suppression du membre d'équipe ID: {}", memberId);
        try {
            teamService.removeMember(memberId);
            log.info("Membre d'équipe ID: {} supprimé avec succès", memberId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Membre d'équipe non trouvé - ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Requête invalide lors de la suppression du membre ID: {}: {}", memberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression du membre ID: {}: {}", memberId, e.getMessage());
            throw e;
        }
    }
}
