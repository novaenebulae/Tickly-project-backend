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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<TeamMemberDto> getTeamMembers(Long structureId) {
        List<TeamMember> members = memberRepository.findByTeamStructureId(structureId);
        return memberMapper.toDtoList(members, fileStorageService);
    }

    @Override
    public void inviteMember(Long structureId, InviteMemberRequestDto inviteDto) {
        User inviter = authUtils.getCurrentAuthenticatedUser();
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        Team team = teamRepository.findByStructureId(structureId).orElseGet(() -> {
            log.info("Création d'une nouvelle équipe pour la structure {}", structure.getName());
            Team newTeam = new Team();
            newTeam.setName("Équipe " + structure.getName());
            newTeam.setStructure(structure);
            return teamRepository.save(newTeam);
        });

        if (!userRepository.existsByEmail(inviteDto.getEmail()) || !userRepository.findByEmail(inviteDto.getEmail()).get().isEmailValidated()) {
            throw new ResourceNotFoundException("L'utilisateur avec l'email" + inviteDto.getEmail() + " n'existe pas.");
        }

        if (memberRepository.existsByTeamIdAndEmail(team.getId(), inviteDto.getEmail())) {
            throw new BadRequestException("Un membre avec cet email existe déjà ou a déjà été invité dans cette équipe.");
        }

        if (!userRepository.findByEmail(inviteDto.getEmail()).get().getRole().equals(UserRole.SPECTATOR)) {
            throw new BadRequestException("L'utilisateur avec cet email est déja relié a une structrure");
        }

        TeamMember newMember = new TeamMember();
        newMember.setTeam(team);
        newMember.setEmail(inviteDto.getEmail());
        newMember.setRole(inviteDto.getRole());
        newMember.setStatus(TeamMemberStatus.PENDING_INVITATION);

        userRepository.findByEmail(inviteDto.getEmail()).ifPresent(newMember::setUser);

        TeamMember savedMember = memberRepository.save(newMember);

        String payload = "{\"memberId\": " + savedMember.getId() + "}";

        VerificationToken invitationToken = tokenService.createToken(savedMember.getUser(), TokenType.TEAM_INVITATION, Duration.ofDays(7), payload);

        String invitationLink = "/team/accept-invitation?token=" + invitationToken.getToken();
        mailingService.sendTeamInvitation(inviteDto.getEmail(), inviter.getFirstName(), structure.getName(), invitationLink);

        log.info("Invitation envoyée à {} pour rejoindre l'équipe de la structure {}", inviteDto.getEmail(), structureId);
    }

    @Override
    @Transactional
    public InvitationAcceptanceResponseDto acceptInvitation(String token) {
        // 1. Valider le token d'invitation
        VerificationToken invitationToken = tokenService.validateToken(token, TokenType.TEAM_INVITATION);

        // 2. Parser le payload pour récupérer le memberId
        String payload = invitationToken.getPayload();
        if (payload == null || payload.isEmpty()) {
            throw new InvalidTokenException("Token d'invitation invalide : payload manquant.");
        }

        // Parser le JSON payload pour extraire le memberId
        Long memberId;
        try {
            // Le payload est au format {"memberId": 123}
            memberId = Long.parseLong(payload.replaceAll(".*\"memberId\"\\s*:\\s*(\\d+).*", "$1"));
        } catch (Exception e) {
            throw new InvalidTokenException("Format de payload invalide dans le token d'invitation.");
        }

        // 3. Récupérer le membre d'équipe correspondant
        TeamMember invitation = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidTokenException("Invitation introuvable ou expirée."));

        // 4. Récupérer l'utilisateur via le token (au lieu de currentUser)
        User currentUser = invitationToken.getUser();
        if (currentUser == null) {
            throw new InvalidTokenException("Utilisateur associé au token introuvable.");
        }

        // 5. Vérifier que l'email correspond
        if (!invitation.getEmail().equals(currentUser.getEmail())) {
            throw new BadRequestException("Cette invitation n'est pas destinée à votre adresse email.");
        }

        // 6. Vérifier le statut de l'invitation
        if (invitation.getStatus() != TeamMemberStatus.PENDING_INVITATION) {
            throw new BadRequestException("Cette invitation n'est plus valide ou a déjà été acceptée.");
        }

        // 7. Récupérer les informations de la structure
        Structure structure = invitation.getTeam().getStructure();
        UserRole newRole = invitation.getRole();

        // 8. MISE À JOUR DIRECTE : Transformer l'utilisateur via requête native
        String discriminatorValue = getDiscriminatorValueForRole(newRole);
        int updateCount = userRepository.updateUserTypeAndStructure(
                currentUser.getId(),
                discriminatorValue,
                newRole.name(),
                structure.getId()
        );

        if (updateCount != 1) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur.");
        }

        // 9. IMPORTANT : Recharger l'entité avec un fetch join pour éviter LazyInitializationException
        userRepository.flush();

        User updatedUser = userRepository.findUserWithStructureById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable après mise à jour."));

        // 10. Mettre à jour l'invitation
        invitation.setUser(updatedUser);
        invitation.setStatus(TeamMemberStatus.ACTIVE);
        invitation.setJoinedAt(Instant.now());
        memberRepository.save(invitation);

        // 11. Marquer le token comme utilisé
        tokenService.markTokenAsUsed(invitationToken);

        // 12. GÉNÉRER LE JWT AVEC L'UTILISATEUR TRANSFORMÉ
        String newJwtToken = jwtTokenProvider.generateToken(updatedUser);

        log.info("Invitation acceptée pour {} dans la structure {} avec le rôle {}. Token généré avec structureId: {}",
                updatedUser.getEmail(), structure.getName(), newRole,
                updatedUser.getStructure() != null ? updatedUser.getStructure().getId() : "N/A");

        // 13. Retourner la réponse complète
        return new InvitationAcceptanceResponseDto(
                newJwtToken,
                jwtTokenProvider.getExpirationInMillis(),
                structure.getId(),
                structure.getName(),
                "Invitation acceptée avec succès ! Vous êtes maintenant membre de l'équipe."
        );
    }

    @Override
    public TeamMemberDto updateMemberRole(Long memberId, UpdateMemberRoleDto roleDto) {
        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre d'équipe", "id", memberId));

        if (member.getUser() != null && member.getTeam().getStructure().getAdministrator().getId().equals(member.getUser().getId())) {
            throw new BadRequestException("Le rôle de l'administrateur principal de la structure ne peut pas être modifié. " +
                    "Pour transférer la propriété de la structure, vous devez d'abord :\n" +
                    "1. Promouvoir un autre membre de l'équipe au rôle d'administrateur\n" +
                    "2. Contacter le support pour effectuer un transfert de propriété\n" +
                    "Cette restriction existe pour garantir qu'une structure a toujours un propriétaire désigné.");
        }

        member.setRole(roleDto.getRole());

        if (member.getUser() != null) {
            // MISE À JOUR DIRECTE : Mettre à jour le rôle ET le discriminateur via requête native
            String discriminatorValue = getDiscriminatorValueForRole(roleDto.getRole());
            int updateCount = userRepository.updateUserTypeAndRole(
                    member.getUser().getId(),
                    discriminatorValue,
                    roleDto.getRole().name()
            );

            if (updateCount != 1) {
                throw new RuntimeException("Erreur lors de la mise à jour du rôle de l'utilisateur.");
            }

            // Forcer le rafraîchissement
            userRepository.flush();

            log.info("Rôle de l'utilisateur {} mis à jour : {} -> {}",
                    member.getUser().getEmail(), member.getUser().getRole(), roleDto.getRole());
        }

        TeamMember updatedMember = memberRepository.save(member);
        return memberMapper.toDto(updatedMember, fileStorageService);
    }

    @Override
    public void removeMember(Long memberId) {
        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre d'équipe", "id", memberId));

        if (member.getUser() != null && member.getTeam().getStructure().getAdministrator().getId().equals(member.getUser().getId())) {
            throw new BadRequestException("L'administrateur principal de la structure ne peut pas être supprimé de l'équipe. " +
                    "Si vous souhaitez quitter l'équipe en tant qu'administrateur principal, vous devez d'abord :\n" +
                    "1. Promouvoir un autre membre de l'équipe au rôle d'administrateur\n" +
                    "2. Contacter le support pour effectuer un transfert de propriété\n" +
                    "3. Ou supprimer complètement la structure si elle n'est plus utilisée\n" +
                    "Cette restriction garantit qu'une structure active a toujours un propriétaire désigné.");
        }

        if (member.getUser() != null) {
            User user = member.getUser();
            log.info("Conversion du membre {} (rôle: {}) en SPECTATOR", user.getEmail(), user.getRole());

            // MISE À JOUR DIRECTE : Convertir l'utilisateur en Spectator via requête native
            int updateCount = userRepository.convertUserToSpectator(user.getId());

            if (updateCount != 1) {
                throw new RuntimeException("Erreur lors de la conversion de l'utilisateur en Spectator.");
            }

            // Forcer le rafraîchissement
            userRepository.flush();

            log.info("Utilisateur {} converti avec succès en SPECTATOR", user.getEmail());
        }

        memberRepository.delete(member);
        log.info("Membre d'équipe ID {} supprimé.", memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAdminsForStructure(Long structureId) {
        return memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
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
    @Transactional
    public void dissolveTeam(Long structureId) {
        log.info("Début de la dissolution de l'équipe pour la structure ID: {}", structureId);

        // Vérifier si la structure existe
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        // Récupérer tous les membres de l'équipe
        List<TeamMember> teamMembers = memberRepository.findByTeamStructureId(structureId);

        if (teamMembers.isEmpty()) {
            log.info("Aucun membre d'équipe trouvé pour la structure ID: {}", structureId);
            return;
        }

        log.info("Conversion de {} membres d'équipe en SPECTATOR pour la structure ID: {}",
                teamMembers.size(), structureId);

        // CONVERSION EN LOT : Convertir tous les utilisateurs de la structure en Spectator
        int convertedCount = userRepository.convertAllStructureUsersToSpectator(structureId);
        log.info("{} utilisateurs convertis en SPECTATOR pour la structure ID: {}", convertedCount, structureId);

        // Supprimer tous les membres de l'équipe
        memberRepository.deleteAll(teamMembers);

        // Forcer le rafraîchissement
        userRepository.flush();

        // Supprimer l'équipe elle-même
        teamRepository.findByStructureId(structureId).ifPresent(team -> {
            log.info("Suppression de l'équipe ID: {} pour la structure ID: {}", team.getId(), structureId);
            teamRepository.delete(team);
        });

        log.info("Dissolution de l'équipe terminée pour la structure ID: {}. {} membres convertis en SPECTATOR.",
                structureId, convertedCount);
    }
}
