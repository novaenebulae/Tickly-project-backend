package edu.cda.project.ticklybackend.services.impl;

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
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
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
import java.util.Map;

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

        if (memberRepository.existsByTeamIdAndEmail(team.getId(), inviteDto.getEmail())) {
            throw new BadRequestException("Un membre avec cet email existe déjà ou a déjà été invité dans cette équipe.");
        }

        TeamMember newMember = new TeamMember();
        newMember.setTeam(team);
        newMember.setEmail(inviteDto.getEmail());
        newMember.setRole(inviteDto.getRole());
        newMember.setStatus(TeamMemberStatus.PENDING_INVITATION);

        userRepository.findByEmail(inviteDto.getEmail()).ifPresent(newMember::setUser);

        TeamMember savedMember = memberRepository.save(newMember);

        String payload = "{\"memberId\": " + savedMember.getId() + "}";

        // CORRECTION FINALE : On passe l'utilisateur trouvé (qui peut être null) au service.
        // La nouvelle logique dans TokenServiceImpl gérera ce cas correctement.
        VerificationToken invitationToken = tokenService.createToken(savedMember.getUser(), TokenType.TEAM_INVITATION, Duration.ofDays(7), payload);

        String invitationLink = "/team/accept-invitation?token=" + invitationToken.getToken();
        mailingService.sendTeamInvitation(inviteDto.getEmail(), inviter.getFirstName(), structure.getName(), invitationLink);

        log.info("Invitation envoyée à {} pour rejoindre l'équipe de la structure {}", inviteDto.getEmail(), structureId);
    }

    @Override
    public void acceptInvitation(String tokenString, User acceptingUser) {
        VerificationToken token = tokenService.validateToken(tokenString, TokenType.TEAM_INVITATION);

        Long memberId;
        try {
            Map<String, Object> payload = tokenService.getPayload(token);
            memberId = ((Number) payload.get("memberId")).longValue();
        } catch (Exception e) {
            throw new BadRequestException("Payload du token d'invitation invalide.");
        }

        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", memberId));

        if (!member.getEmail().equalsIgnoreCase(acceptingUser.getEmail())) {
            throw new InvalidTokenException("Cette invitation est destinée à un autre utilisateur.");
        }
        if (member.getStatus() == TeamMemberStatus.ACTIVE) {
            throw new BadRequestException("Cette invitation a déjà été acceptée.");
        }

        member.setUser(acceptingUser);
        member.setStatus(TeamMemberStatus.ACTIVE);
        member.setJoinedAt(Instant.now());

        acceptingUser.setRole(member.getRole());
        if (acceptingUser instanceof StaffUser) {
            ((StaffUser) acceptingUser).setStructure(member.getTeam().getStructure());
        } else {
            log.warn("Tentative d'assigner un rôle de staff à un utilisateur qui n'est pas un StaffUser. L'association à la structure pourrait ne pas fonctionner comme prévu.");
        }

        userRepository.save(acceptingUser);
        memberRepository.save(member);
        tokenService.markTokenAsUsed(token);

        log.info("L'utilisateur {} a rejoint l'équipe de la structure {}", acceptingUser.getEmail(), member.getTeam().getStructure().getId());
    }

    @Override
    public TeamMemberDto updateMemberRole(Long memberId, UpdateMemberRoleDto roleDto) {
        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre d'équipe", "id", memberId));

        if (member.getUser() != null && member.getTeam().getStructure().getAdministrator().getId().equals(member.getUser().getId())) {
            throw new BadRequestException("Le rôle de l'administrateur principal de la structure ne peut pas être modifié.");
        }

        member.setRole(roleDto.getRole());
        if (member.getUser() != null) {
            member.getUser().setRole(roleDto.getRole());
            userRepository.save(member.getUser());
        }
        TeamMember updatedMember = memberRepository.save(member);
        return memberMapper.toDto(updatedMember, fileStorageService);
    }

    @Override
    public void removeMember(Long memberId) {
        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre d'équipe", "id", memberId));

        if (member.getUser() != null && member.getTeam().getStructure().getAdministrator().getId().equals(member.getUser().getId())) {
            throw new BadRequestException("L'administrateur principal de la structure ne peut pas être supprimé de l'équipe.");
        }

        if (member.getUser() != null) {
            User user = member.getUser();
            user.setRole(UserRole.SPECTATOR);
            if (user instanceof StaffUser) {
                ((StaffUser) user).setStructure(null);
            }
            userRepository.save(user);
        }

        memberRepository.delete(member);
        log.info("Membre d'équipe ID {} supprimé.", memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAdminsForStructure(Long structureId) {
        return memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
    }
}