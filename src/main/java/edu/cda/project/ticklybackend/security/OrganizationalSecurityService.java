package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service("organizationalSecurityService")
@RequiredArgsConstructor
public class OrganizationalSecurityService {

    private final TeamMemberRepository teamMemberRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        return null;
    }

    private boolean hasActiveRole(Long userId, Long structureId, UserRole... allowedRoles) {
        if (userId == null || structureId == null) {
            return false;
        }
        return teamMemberRepository.findByUserIdAndStructureId(userId, structureId)
                .map(member -> member.getStatus() == TeamMemberStatus.ACTIVE
                        && Arrays.asList(allowedRoles).contains(member.getRole()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessStructure(Long structureId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        // Access for active staff roles (admin, organization, reservation)
        return hasActiveRole(
                userId,
                structureId,
                UserRole.STRUCTURE_ADMINISTRATOR,
                UserRole.ORGANIZATION_SERVICE,
                UserRole.RESERVATION_SERVICE
        );
    }

    @Transactional(readOnly = true)
    public boolean isStructureAdmin(Long structureId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        return hasActiveRole(userId, structureId, UserRole.STRUCTURE_ADMINISTRATOR);
    }

    @Transactional(readOnly = true)
    public boolean canInviteToTeam(Long structureId, Authentication authentication) {
        // For now, inviting is restricted to structure admins
        return isStructureAdmin(structureId, authentication);
    }

    @Transactional(readOnly = true)
    public boolean canModifyStructure(Long structureId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        return hasActiveRole(userId, structureId, UserRole.STRUCTURE_ADMINISTRATOR, UserRole.ORGANIZATION_SERVICE);
    }

    @Transactional(readOnly = true)
    public boolean canManageTeamMember(Long memberId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (memberId == null || userId == null) {
            return false;
        }
        return teamMemberRepository.findById(memberId)
                .map(member -> {
                    Long structureId = member.getStructure().getId();
                    boolean isAdminOfStructure = isStructureAdmin(structureId, authentication);

                    // Avoid self-management if the member is linked to a user
                    boolean isNotSelf = member.getUser() == null || !userId.equals(member.getUser().getId());
                    return isAdminOfStructure && isNotSelf;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canCreateStructure(Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (userId == null || !(authentication.getPrincipal() instanceof User user)) {
            return false;
        }
        if (!user.isEmailValidated()) {
            return false;
        }
        // user can create a structure if not already ACTIVE member of any structure
        return teamMemberRepository.findFirstByUserIdAndStatusOrderByJoinedAtDesc(userId, TeamMemberStatus.ACTIVE).isEmpty();
    }

    // ===== Event validators =====
    @Transactional(readOnly = true)
    public boolean canAccessEventStatistics(Long eventId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (eventId == null || userId == null) return false;
        return eventRepository.findById(eventId)
                .map(event -> {
                    Long structureId = event.getStructure().getId();
                    return hasActiveRole(userId, structureId,
                            UserRole.STRUCTURE_ADMINISTRATOR,
                            UserRole.ORGANIZATION_SERVICE,
                            UserRole.RESERVATION_SERVICE);
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canModifyEvent(Long eventId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (eventId == null || userId == null) return false;
        return eventRepository.findById(eventId)
                .map(event -> canModifyStructure(event.getStructure().getId(), authentication))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canValidateEventTickets(Long eventId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (eventId == null || userId == null) return false;
        return eventRepository.findById(eventId)
                .map(event -> {
                    Long structureId = event.getStructure().getId();
                    boolean hasRole = hasActiveRole(userId, structureId,
                            UserRole.STRUCTURE_ADMINISTRATOR,
                            UserRole.ORGANIZATION_SERVICE,
                            UserRole.RESERVATION_SERVICE);
                    if (!hasRole) return false;
                    // Ensure event not finished
                    Instant now = Instant.now();
                    return event.getEndDate() == null || now.isBefore(event.getEndDate());
                })
                .orElse(false);
    }

    // ===== Ticket validators =====
    @Transactional(readOnly = true)
    public boolean isTicketOwner(UUID ticketId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (ticketId == null || userId == null) return false;
        return ticketRepository.findById(ticketId)
                .map(Ticket::getUser)
                .map(User::getId)
                .map(ownerId -> ownerId.equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canValidateTicket(String qrCodeValue, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (qrCodeValue == null || qrCodeValue.isBlank() || userId == null) return false;
        return ticketRepository.findByQrCodeValue(qrCodeValue)
                .map(ticket -> canValidateEventTickets(ticket.getEvent().getId(), authentication))
                .orElse(false);
    }
}
