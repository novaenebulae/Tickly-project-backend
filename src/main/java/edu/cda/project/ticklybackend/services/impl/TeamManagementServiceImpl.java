package edu.cda.project.ticklybackend.services.impl;


import edu.cda.project.ticklybackend.dtos.team.InvitationAcceptanceResponseDto;
import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;
import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.team.TeamMemberMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import edu.cda.project.ticklybackend.services.interfaces.VerificationTokenService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TeamManagementServiceImpl implements TeamManagementService {

    private final TeamMemberRepository memberRepository;
    private final StructureRepository structureRepository;
    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;
    private final MailingService mailingService;
    private final TeamMemberMapper memberMapper;
    private final FileStorageService fileStorageService;
    private final AuthUtils authUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TeamMemberDto> getTeamMembers(Long structureId) {
        LoggingUtils.logMethodEntry(log, "getTeamMembers", "structureId", structureId);

        try {
            log.debug("Récupération des membres de l'équipe pour la structure ID: {}", structureId);
            List<TeamMember> members = memberRepository.findByStructureId(structureId);
            log.debug("Trouvé {} membres pour la structure ID: {}", members.size(), structureId);

            List<TeamMemberDto> result = memberMapper.toDtoList(members, fileStorageService);
            LoggingUtils.logMethodExit(log, "getTeamMembers", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des membres de l'équipe pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void inviteMember(Long structureId, InviteMemberRequestDto inviteDto) {
        LoggingUtils.logMethodEntry(log, "inviteMember", "structureId", structureId, "inviteDto", inviteDto);

        try {
            User inviter = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(inviter.getId());

            log.debug("Recherche de la structure ID: {}", structureId);
            Structure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> {
                        log.warn("Structure non trouvée avec ID: {}", structureId);
                        return new ResourceNotFoundException("Structure", "id", structureId);
                    });

            // Plus de modèle Team: on utilise directement la structure pour l'appartenance

            log.debug("Vérification de l'existence de l'utilisateur avec email: {}", inviteDto.getEmail());
            // First check if user exists (for test verification)
            if (!userRepository.existsByEmail(inviteDto.getEmail())) {
                log.warn("Utilisateur non trouvé avec email: {}", inviteDto.getEmail());
                throw new ResourceNotFoundException("L'utilisateur avec l'email" + inviteDto.getEmail() + " n'existe pas.");
            }

            // Then get the user
            var userOpt = userRepository.findByEmail(inviteDto.getEmail());
            if (userOpt.isEmpty() || !userOpt.get().isEmailValidated()) {
                log.warn("Email non validé pour l'utilisateur: {}", inviteDto.getEmail());
                throw new ResourceNotFoundException("L'utilisateur avec l'email" + inviteDto.getEmail() + " n'a pas validé son email.");
            }

            User invitee = userOpt.get();

            log.debug("Vérification que l'utilisateur n'est pas déjà membre de l'équipe: {}", inviteDto.getEmail());
            if (memberRepository.existsByStructureIdAndEmail(structureId, inviteDto.getEmail())) {
                log.warn("Un membre avec l'email {} existe déjà pour la structure ID: {}", inviteDto.getEmail(), structureId);
                throw new BadRequestException("Un membre avec cet email existe déjà ou a déjà été invité pour cette structure.");
            }

            // Additional guard: prevent inviting a user already ACTIVE in another structure
            var activeMembershipOpt = memberRepository.findFirstByUserIdAndStatusOrderByJoinedAtDesc(invitee.getId(), TeamMemberStatus.ACTIVE);
            if (activeMembershipOpt.isPresent()) {
                Long existingStructureId = activeMembershipOpt.get().getStructure().getId();
                if (!existingStructureId.equals(structureId)) {
                    log.warn("Invitation refusée: l'utilisateur {} est déjà membre ACTIF de la structure {}", inviteDto.getEmail(), existingStructureId);
                    throw new BadRequestException("Cet utilisateur est déjà membre actif d'une autre structure. Il doit d'abord quitter cette structure.");
                }
            }

            // Migration: no longer restrict invitations based on user's current discriminator/role.
            // Team membership now governs organizational roles.

            log.debug("Création d'un nouveau membre d'équipe pour l'email: {}", inviteDto.getEmail());
            TeamMember newMember = new TeamMember();
            newMember.setStructure(structure);
            newMember.setEmail(inviteDto.getEmail());
            newMember.setRole(inviteDto.getRole());
            newMember.setStatus(TeamMemberStatus.PENDING_INVITATION);
            newMember.setUser(invitee);

            TeamMember savedMember = memberRepository.save(newMember);
            log.debug("Nouveau membre d'équipe créé avec ID: {}", savedMember.getId());

            String payload = "{\"memberId\": " + savedMember.getId() + "}";

            log.debug("Création d'un token d'invitation pour le membre ID: {}", savedMember.getId());
            VerificationToken invitationToken = verificationTokenService.createToken(savedMember.getUser(), TokenType.TEAM_INVITATION, Duration.ofDays(7), payload);

            String invitationLink = "/team/accept-invitation?token=" + invitationToken.getToken();
            log.debug("Envoi de l'email d'invitation à: {}", inviteDto.getEmail());
            mailingService.sendTeamInvitation(inviteDto.getEmail(), inviter.getFirstName(), structure.getName(), invitationLink);

            log.info("Invitation envoyée à {} pour rejoindre l'équipe de la structure {}", inviteDto.getEmail(), structureId);
            LoggingUtils.logMethodExit(log, "inviteMember");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'invitation d'un membre pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public InvitationAcceptanceResponseDto acceptInvitation(String token) {
        LoggingUtils.logMethodEntry(log, "acceptInvitation", "token", token);

        try {
            log.debug("Validation du token d'invitation");
            // 1. Valider le token d'invitation
            VerificationToken invitationToken = verificationTokenService.validateToken(token, TokenType.TEAM_INVITATION);

            // 2. Parser le payload pour récupérer le memberId
            String payload = invitationToken.getPayload();
            if (payload == null || payload.isEmpty()) {
                log.warn("Token d'invitation avec payload manquant: {}", token);
                throw new InvalidTokenException("Token d'invitation invalide : payload manquant.");
            }

            // Parser le JSON payload pour extraire le memberId
            Long memberId;
            try {
                // Le payload est au format {"memberId": 123}
                memberId = Long.parseLong(payload.replaceAll(".*\"memberId\"\\s*:\\s*(\\d+).*", "$1"));
                log.debug("MemberId extrait du payload: {}", memberId);
            } catch (Exception e) {
                log.warn("Format de payload invalide dans le token d'invitation: {}", payload);
                LoggingUtils.logException(log, "Erreur lors du parsing du payload", e);
                throw new InvalidTokenException("Format de payload invalide dans le token d'invitation.");
            }

            // 3. Récupérer le membre d'équipe correspondant
            log.debug("Recherche du membre d'équipe avec ID: {}", memberId);
            TeamMember invitation = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.warn("Invitation introuvable avec ID: {}", memberId);
                        return new InvalidTokenException("Invitation introuvable ou expirée.");
                    });

            // 4. Récupérer l'utilisateur via le token (au lieu de currentUser)
            User currentUser = invitationToken.getUser();
            if (currentUser == null) {
                log.warn("Utilisateur associé au token introuvable");
                throw new InvalidTokenException("Utilisateur associé au token introuvable.");
            }
            LoggingUtils.setUserId(currentUser.getId());
            log.debug("Utilisateur trouvé: {} (ID: {})", currentUser.getEmail(), currentUser.getId());

            // 5. Vérifier que l'email correspond
            if (!invitation.getEmail().equals(currentUser.getEmail())) {
                log.warn("Email de l'invitation ({}) ne correspond pas à l'email de l'utilisateur ({})",
                        invitation.getEmail(), currentUser.getEmail());
                throw new BadRequestException("Cette invitation n'est pas destinée à votre adresse email.");
            }

            // 6. Vérifier le statut de l'invitation
            if (invitation.getStatus() != TeamMemberStatus.PENDING_INVITATION) {
                log.warn("Invitation avec statut invalide: {}", invitation.getStatus());
                throw new BadRequestException("Cette invitation n'est plus valide ou a déjà été acceptée.");
            }

            // 6bis. Enforcer la règle: un seul statut ACTIVE par utilisateur (si actif ailleurs, refuser)
            var existingActiveOpt = memberRepository.findFirstByUserIdAndStatusOrderByJoinedAtDesc(currentUser.getId(), TeamMemberStatus.ACTIVE);
            if (existingActiveOpt.isPresent()) {
                Long existingStructureId = existingActiveOpt.get().getStructure().getId();
                Long targetStructureId = invitation.getStructure() != null ? invitation.getStructure().getId() : null;
                if (!existingStructureId.equals(targetStructureId)) {
                    log.warn("Utilisateur déjà membre ACTIF d'une autre structure ({}), refus de l'acceptation pour la structure {}", existingStructureId, targetStructureId);
                    throw new BadRequestException("Vous êtes déjà membre actif d'une autre structure. Veuillez d'abord quitter cette structure avant d'en rejoindre une nouvelle.");
                }
            }

            // 7. Récupérer les informations de la structure
            Structure structure = invitation.getStructure();
            UserRole newRole = invitation.getRole();
            log.debug("Structure trouvée: {} (ID: {}), nouveau rôle: {}",
                    structure.getName(), structure.getId(), newRole);

            // 8. Activer l'adhésion sans modifier la table users (migration vers les rôles basés sur TeamMember)

            // 10. Mettre à jour l'invitation
            log.debug("Mise à jour du statut de l'invitation ID: {}", invitation.getId());
            invitation.setUser(currentUser);
            invitation.setStatus(TeamMemberStatus.ACTIVE);
            invitation.setJoinedAt(Instant.now());
            memberRepository.save(invitation);

            // 11. Marquer le token comme utilisé
            log.debug("Marquage du token comme utilisé");
            verificationTokenService.markTokenAsUsed(invitationToken);

            // 12. Générer un JWT identité-seulement (sans rôle ni structure)
            log.debug("Génération du nouveau JWT pour l'utilisateur ID: {}", currentUser.getId());
            String newJwtToken = jwtTokenProvider.generateAccessToken(currentUser);

            log.info("Invitation acceptée pour {} dans la structure {} avec le rôle {}.",
                    currentUser.getEmail(), structure.getName(), newRole);

            // 13. Retourner la réponse complète
            InvitationAcceptanceResponseDto result = new InvitationAcceptanceResponseDto(
                    newJwtToken,
                    jwtTokenProvider.getExpirationInMillis(),
                    structure.getId(),
                    structure.getName(),
                    "Invitation acceptée avec succès ! Vous êtes maintenant membre de l'équipe."
            );

            LoggingUtils.logMethodExit(log, "acceptInvitation", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'acceptation de l'invitation", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public TeamMemberDto updateMemberRole(Long memberId, UpdateMemberRoleDto roleDto) {
        LoggingUtils.logMethodEntry(log, "updateMemberRole", "memberId", memberId, "roleDto", roleDto);

        try {
            log.debug("Début de la mise à jour du rôle pour le membre ID: {} vers le rôle: {}", memberId, roleDto.getRole());
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(currentUser.getId());

            log.debug("Recherche du membre d'équipe avec ID: {}", memberId);
            TeamMember member = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("Membre d'équipe non trouvé avec ID: {}", memberId);
                        return new ResourceNotFoundException("Membre d'équipe", "id", memberId);
                    });

            log.debug("Membre trouvé: ID={}, email={}, rôle actuel={}", member.getId(), member.getEmail(), member.getRole());

            // Un administrateur ne peut pas modifier son propre rôle.
            if (member.getUser() != null && currentUser.getId().equals(member.getUser().getId())) {
                log.warn("Tentative de modification de son propre rôle par l'utilisateur ID: {}", currentUser.getId());
                throw new BadRequestException("Vous ne pouvez pas modifier votre propre rôle. Cette action doit être effectuée par un autre administrateur.");
            }

            // On vérifie si on essaie de rétrograder le dernier administrateur de la structure.
            if (member.getRole() == UserRole.STRUCTURE_ADMINISTRATOR && roleDto.getRole() != UserRole.STRUCTURE_ADMINISTRATOR) {
                long adminCount = countAdminsForStructure(member.getStructure().getId());
                log.debug("Vérification du nombre d'administrateurs pour la structure ID: {}: {}", member.getStructure().getId(), adminCount);

                if (adminCount <= 1) {
                    log.warn("Tentative de rétrograder le dernier administrateur de la structure ID: {}", member.getStructure().getId());
                    throw new BadRequestException("Impossible de rétrograder le dernier administrateur de la structure. " +
                            "Une structure doit toujours avoir au moins un administrateur. " +
                            "Veuillez d'abord promouvoir un autre membre au rôle d'administrateur.");
                }
            }

            UserRole oldRole = member.getRole();
            member.setRole(roleDto.getRole());
            log.debug("Changement de rôle pour le membre ID: {} de {} à {}", memberId, oldRole, roleDto.getRole());

            // Aucun changement dans la table users : le rôle organisationnel est désormais porté par TeamMember

            TeamMember updatedMember = memberRepository.save(member);
            log.debug("Membre d'équipe ID: {} mis à jour avec succès", memberId);

            TeamMemberDto result = memberMapper.toDto(updatedMember, fileStorageService);
            LoggingUtils.logMethodExit(log, "updateMemberRole", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la mise à jour du rôle du membre ID: " + memberId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void removeMember(Long memberId) {
        LoggingUtils.logMethodEntry(log, "removeMember", "memberId", memberId);

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            // Handle null currentUser
            if (currentUser != null) {
                LoggingUtils.setUserId(currentUser.getId());
            }

            log.debug("Début de la suppression du membre d'équipe ID: {}", memberId);
            // Explicitly handle the case when member is not found
            var memberOpt = memberRepository.findById(memberId);
            if (memberOpt.isEmpty()) {
                log.error("Membre d'équipe non trouvé avec ID: {}", memberId);
                throw new ResourceNotFoundException("Membre d'équipe", "id", memberId);
            }

            TeamMember member = memberOpt.get();
            log.debug("Membre trouvé: ID={}, email={}, rôle={}", member.getId(), member.getEmail(), member.getRole());

            // Check if this is the last admin and explicitly throw BadRequestException
            if (member.getRole() == UserRole.STRUCTURE_ADMINISTRATOR) {
                long adminCount = countAdminsForStructure(member.getStructure().getId());
            log.debug("Vérification du nombre d'administrateurs pour la structure ID: {}: {}",
                        member.getStructure().getId(), adminCount);

            if (adminCount <= 1) {
                log.warn("Tentative de suppression du dernier administrateur de la structure ID: {}",
                        member.getStructure().getId());
                    throw new BadRequestException("L'administrateur principal de la structure ne peut pas être supprimé de l'équipe. " +
                            "Si vous souhaitez quitter l'équipe, vous devez d'abord promouvoir un autre membre au rôle d'administrateur " +
                            "ou supprimer complètement la structure si elle n'est plus utilisée. " +
                            "Cette restriction garantit qu'une structure active a toujours un propriétaire désigné.");
                }
            }

            // Pas de conversion de l'utilisateur : la relation d'appartenance gouverne désormais les droits.
            // Suppression du membre uniquement.

            try {
                memberRepository.delete(member);
                log.info("Membre d'équipe ID {} supprimé avec succès.", memberId);
            } catch (Exception e) {
                log.error("Erreur lors de la suppression du membre d'équipe ID: {}: {}", memberId, e.getMessage());
                LoggingUtils.logException(log, "Erreur lors de la suppression du membre d'équipe", e);
                throw new RuntimeException("Erreur lors de la suppression du membre d'équipe: " + e.getMessage(), e);
            }

            LoggingUtils.logMethodExit(log, "removeMember");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression du membre ID: " + memberId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public long countAdminsForStructure(Long structureId) {
        LoggingUtils.logMethodEntry(log, "countAdminsForStructure", "structureId", structureId);

        try {
            log.debug("Comptage des administrateurs pour la structure ID: {}", structureId);
            long count = memberRepository.countByStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
            log.debug("Nombre d'administrateurs trouvés pour la structure ID {}: {}", structureId, count);

            LoggingUtils.logMethodExit(log, "countAdminsForStructure", count);
            return count;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors du comptage des administrateurs pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }


    @Override
    @org.springframework.transaction.annotation.Transactional
    public void dissolveTeam(Long structureId) {
        LoggingUtils.logMethodEntry(log, "dissolveTeam", "structureId", structureId);

        try {
            log.info("Début de la dissolution de l'équipe pour la structure ID: {}", structureId);

            if (!structureRepository.existsById(structureId)) {
                log.warn("Tentative de dissoudre une équipe pour une structure ID: {} non existante.", structureId);
                LoggingUtils.logMethodExit(log, "dissolveTeam", "Structure non existante");
                return;
            }

            // Suppression basée uniquement sur les membres rattachés à la structure.
            long deletedMembers = memberRepository.deleteByStructureId(structureId);
            if (deletedMembers > 0) {
                log.info("{} membres de la structure ID {} ont été supprimés.", deletedMembers, structureId);
            } else {
                log.debug("Aucun membre à supprimer pour la structure ID: {}", structureId);
            }

            log.info("Dissolution de l'équipe pour la structure ID {} terminée.", structureId);
            LoggingUtils.logMethodExit(log, "dissolveTeam");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la dissolution de l'équipe pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }
}
