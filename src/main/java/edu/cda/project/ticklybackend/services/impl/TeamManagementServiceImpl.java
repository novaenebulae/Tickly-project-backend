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
import edu.cda.project.ticklybackend.models.team.Team;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TeamManagementService;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
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

    private final TeamRepository teamRepository;
    private final TeamMemberRepository memberRepository;
    private final StructureRepository structureRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
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
            List<TeamMember> members = memberRepository.findByTeamStructureId(structureId);
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

            log.debug("Recherche ou création de l'équipe pour la structure ID: {}", structureId);
            Team team = teamRepository.findByStructureId(structureId).orElseGet(() -> {
                log.info("Création d'une nouvelle équipe pour la structure {}", structure.getName());
                Team newTeam = new Team();
                newTeam.setName("Équipe " + structure.getName());
                newTeam.setStructure(structure);
                return teamRepository.save(newTeam);
            });

            log.debug("Vérification de l'existence de l'utilisateur avec email: {}", inviteDto.getEmail());
            if (!userRepository.existsByEmail(inviteDto.getEmail()) || !userRepository.findByEmail(inviteDto.getEmail()).get().isEmailValidated()) {
                log.warn("Utilisateur non trouvé ou email non validé: {}", inviteDto.getEmail());
                throw new ResourceNotFoundException("L'utilisateur avec l'email" + inviteDto.getEmail() + " n'existe pas.");
            }

            log.debug("Vérification que l'utilisateur n'est pas déjà membre de l'équipe: {}", inviteDto.getEmail());
            if (memberRepository.existsByTeamIdAndEmail(team.getId(), inviteDto.getEmail())) {
                log.warn("Un membre avec l'email {} existe déjà dans l'équipe ID: {}", inviteDto.getEmail(), team.getId());
                throw new BadRequestException("Un membre avec cet email existe déjà ou a déjà été invité dans cette équipe.");
            }

            log.debug("Vérification que l'utilisateur est un SPECTATOR: {}", inviteDto.getEmail());
            if (!userRepository.findByEmail(inviteDto.getEmail()).get().getRole().equals(UserRole.SPECTATOR)) {
                log.warn("L'utilisateur avec l'email {} est déjà relié à une structure", inviteDto.getEmail());
                throw new BadRequestException("L'utilisateur avec cet email est déja relié a une structrure");
            }

            log.debug("Création d'un nouveau membre d'équipe pour l'email: {}", inviteDto.getEmail());
            TeamMember newMember = new TeamMember();
            newMember.setTeam(team);
            newMember.setEmail(inviteDto.getEmail());
            newMember.setRole(inviteDto.getRole());
            newMember.setStatus(TeamMemberStatus.PENDING_INVITATION);

            userRepository.findByEmail(inviteDto.getEmail()).ifPresent(newMember::setUser);

            TeamMember savedMember = memberRepository.save(newMember);
            log.debug("Nouveau membre d'équipe créé avec ID: {}", savedMember.getId());

            String payload = "{\"memberId\": " + savedMember.getId() + "}";

            log.debug("Création d'un token d'invitation pour le membre ID: {}", savedMember.getId());
            VerificationToken invitationToken = tokenService.createToken(savedMember.getUser(), TokenType.TEAM_INVITATION, Duration.ofDays(7), payload);

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
            VerificationToken invitationToken = tokenService.validateToken(token, TokenType.TEAM_INVITATION);

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

            // 7. Récupérer les informations de la structure
            Structure structure = invitation.getTeam().getStructure();
            UserRole newRole = invitation.getRole();
            log.debug("Structure trouvée: {} (ID: {}), nouveau rôle: {}", 
                    structure.getName(), structure.getId(), newRole);

            // 8. MISE À JOUR DIRECTE : Transformer l'utilisateur via requête native
            String discriminatorValue = getDiscriminatorValueForRole(newRole);
            log.debug("Mise à jour de l'utilisateur ID: {} vers discriminateur: {}, rôle: {}, structure: {}", 
                    currentUser.getId(), discriminatorValue, newRole.name(), structure.getId());

            int updateCount = userRepository.updateUserTypeAndStructure(
                    currentUser.getId(),
                    discriminatorValue,
                    newRole.name(),
                    structure.getId()
            );

            if (updateCount != 1) {
                log.error("Échec de la mise à jour de l'utilisateur ID: {}", currentUser.getId());
                throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur.");
            }

            // 9. IMPORTANT : Recharger l'entité avec un fetch join pour éviter LazyInitializationException
            userRepository.flush();
            log.debug("Rechargement de l'utilisateur après mise à jour");

            User updatedUser = userRepository.findUserWithStructureById(currentUser.getId())
                    .orElseThrow(() -> {
                        log.error("Utilisateur introuvable après mise à jour ID: {}", currentUser.getId());
                        return new RuntimeException("Utilisateur introuvable après mise à jour.");
                    });

            // 10. Mettre à jour l'invitation
            log.debug("Mise à jour du statut de l'invitation ID: {}", invitation.getId());
            invitation.setUser(updatedUser);
            invitation.setStatus(TeamMemberStatus.ACTIVE);
            invitation.setJoinedAt(Instant.now());
            memberRepository.save(invitation);

            // 11. Marquer le token comme utilisé
            log.debug("Marquage du token comme utilisé");
            tokenService.markTokenAsUsed(invitationToken);

            // 12. GÉNÉRER LE JWT AVEC L'UTILISATEUR TRANSFORMÉ
            log.debug("Génération du nouveau JWT pour l'utilisateur ID: {}", updatedUser.getId());
            String newJwtToken = jwtTokenProvider.generateToken(updatedUser);

            log.info("Invitation acceptée pour {} dans la structure {} avec le rôle {}. Token généré avec structureId: {}",
                    updatedUser.getEmail(), structure.getName(), newRole,
                    updatedUser.getStructure() != null ? updatedUser.getStructure().getId() : "N/A");

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
                long adminCount = countAdminsForStructure(member.getTeam().getStructure().getId());
                log.debug("Vérification du nombre d'administrateurs pour la structure ID: {}: {}", member.getTeam().getStructure().getId(), adminCount);

                if (adminCount <= 1) {
                    log.warn("Tentative de rétrograder le dernier administrateur de la structure ID: {}", member.getTeam().getStructure().getId());
                    throw new BadRequestException("Impossible de rétrograder le dernier administrateur de la structure. " +
                            "Une structure doit toujours avoir au moins un administrateur. " +
                            "Veuillez d'abord promouvoir un autre membre au rôle d'administrateur.");
                }
            }

            UserRole oldRole = member.getRole();
            member.setRole(roleDto.getRole());
            log.debug("Changement de rôle pour le membre ID: {} de {} à {}", memberId, oldRole, roleDto.getRole());

            if (member.getUser() != null) {
                // MISE À JOUR DIRECTE : Mettre à jour le rôle ET le discriminateur via requête native
                String discriminatorValue = getDiscriminatorValueForRole(roleDto.getRole());
                log.debug("Mise à jour du type d'utilisateur ID: {} vers discriminateur: {}, rôle: {}", 
                        member.getUser().getId(), discriminatorValue, roleDto.getRole().name());

                int updateCount = userRepository.updateUserTypeAndRole(
                        member.getUser().getId(),
                        discriminatorValue,
                        roleDto.getRole().name()
                );

                if (updateCount != 1) {
                    log.error("Échec de la mise à jour du rôle de l'utilisateur ID: {}", member.getUser().getId());
                    throw new RuntimeException("Erreur lors de la mise à jour du rôle de l'utilisateur.");
                }

                userRepository.flush();
                log.info("Rôle de l'utilisateur {} mis à jour : {} -> {}",
                        member.getUser().getEmail(), oldRole, roleDto.getRole());
            }

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
            LoggingUtils.setUserId(currentUser.getId());

            log.debug("Début de la suppression du membre d'équipe ID: {}", memberId);
            TeamMember member = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("Membre d'équipe non trouvé avec ID: {}", memberId);
                        return new ResourceNotFoundException("Membre d'équipe", "id", memberId);
                    });

            log.debug("Membre trouvé: ID={}, email={}, rôle={}", member.getId(), member.getEmail(), member.getRole());

            if (member.getRole() == UserRole.STRUCTURE_ADMINISTRATOR) {
                long adminCount = countAdminsForStructure(member.getTeam().getStructure().getId());
                log.debug("Vérification du nombre d'administrateurs pour la structure ID: {}: {}", 
                        member.getTeam().getStructure().getId(), adminCount);

                if (adminCount <= 1) {
                    log.warn("Tentative de suppression du dernier administrateur de la structure ID: {}", 
                            member.getTeam().getStructure().getId());
                    throw new BadRequestException("L'administrateur principal de la structure ne peut pas être supprimé de l'équipe. " +
                            "Si vous souhaitez quitter l'équipe, vous devez d'abord promouvoir un autre membre au rôle d'administrateur " +
                            "ou supprimer complètement la structure si elle n'est plus utilisée. " +
                            "Cette restriction garantit qu'une structure active a toujours un propriétaire désigné.");
                }
            }

            if (member.getUser() != null) {
                User user = member.getUser();
                log.info("Conversion du membre {} (rôle: {}) en SPECTATOR", user.getEmail(), user.getRole());
                try {
                    int updateCount = userRepository.convertUserToSpectator(user.getId());

                    if (updateCount != 1) {
                        log.error("Échec de la conversion de l'utilisateur ID: {} en SPECTATOR", user.getId());
                        throw new RuntimeException("Erreur lors de la conversion de l'utilisateur en Spectator.");
                    }
                    userRepository.flush();
                    log.info("Utilisateur {} converti avec succès en SPECTATOR", user.getEmail());
                } catch (Exception e) {
                    log.error("Erreur lors de la conversion de l'utilisateur ID: {} en SPECTATOR: {}", user.getId(), e.getMessage());
                    LoggingUtils.logException(log, "Erreur lors de la conversion de l'utilisateur en Spectator", e);
                    throw new RuntimeException("Erreur lors de la conversion de l'utilisateur en Spectator: " + e.getMessage(), e);
                }
            }

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
            long count = memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
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

    /**
     * Retourne la valeur du discriminateur JPA pour le rôle donné.
     */
    private String getDiscriminatorValueForRole(UserRole role) {
        switch (role) {
            case STRUCTURE_ADMINISTRATOR:
                return "STRUCTURE_ADMINISTRATOR";
            case ORGANIZATION_SERVICE:
                return "ORGANIZATION_SERVICE";
            case RESERVATION_SERVICE:
                return "RESERVATION_SERVICE";
            default:
                throw new BadRequestException("Rôle d'équipe invalide : " + role);
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

            log.debug("Conversion de tous les utilisateurs de la structure en SPECTATOR");
            int updatedUsers = userRepository.convertAllStructureUsersToSpectator(structureId);
            if (updatedUsers > 0) {
                log.info("{} utilisateurs de la structure ID {} ont été convertis en SPECTATOR.", updatedUsers, structureId);
                userRepository.flush();
            } else {
                log.debug("Aucun utilisateur à convertir pour la structure ID: {}", structureId);
            }

            log.debug("Recherche de l'équipe pour la structure ID: {}", structureId);
            teamRepository.findByStructureId(structureId).ifPresent(team -> {
                log.debug("Suppression des membres de l'équipe ID: {}", team.getId());
                long deletedMembers = memberRepository.deleteByTeamId(team.getId());
                if (deletedMembers > 0) {
                    log.info("{} membres de l'équipe ID {} ont été supprimés.", deletedMembers, team.getId());
                } else {
                    log.debug("Aucun membre à supprimer pour l'équipe ID: {}", team.getId());
                }

                log.debug("Suppression de l'équipe ID: {}", team.getId());
                teamRepository.delete(team);
                log.info("L'équipe ID {} (pour structure ID {}) a été supprimée.", team.getId(), structureId);
            });

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
