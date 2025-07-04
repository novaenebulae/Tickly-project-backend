package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour gérer la logique de sécurité spécifique à la gestion d'équipe.
 * Utilisé principalement dans les annotations @PreAuthorize.
 */
@Service("teamSecurityService")
@RequiredArgsConstructor
public class TeamSecurityService {

    private final StructureRepository structureRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public boolean isStructureAdmin(Long structureId, User user) {
        if (structureId == null || user == null || user.getStructure() == null) {
            return false;
        }
        return user.getRole() == UserRole.STRUCTURE_ADMINISTRATOR &&
                user.getStructure().getId().equals(structureId);
    }

    @Transactional(readOnly = true)
    public boolean isStructureOrganizationService(Long structureId, User user) {
        if (structureId == null || user == null) {
            return false;
        }
        return user.getStructure().getId().equals(structureId) && user.getRole().equals(UserRole.ORGANIZATION_SERVICE);
    }

    @Transactional(readOnly = true)
    public boolean isStructureReservationService(Long structureId, User user) {
        if (structureId == null || user == null) {
            return false;
        }
        return user.getStructure().getId().equals(structureId) && user.getRole().equals(UserRole.RESERVATION_SERVICE);
    }

    /**
     * Vérifie si l'utilisateur authentifié est un gestionnaire d'équipe autorisé pour l'opération.
     * Un gestionnaire est un administrateur de la structure qui ne tente pas de modifier son propre compte.
     *
     * @param memberId    L'ID du membre d'équipe cible de l'opération.
     * @param currentUser L'utilisateur authentifié qui effectue l'action.
     * @return true si l'utilisateur est un gestionnaire autorisé, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isTeamManager(Long memberId, User currentUser) {
        if (memberId == null || currentUser == null || currentUser.getId() == null) {
            return false;
        }

        return teamMemberRepository.findById(memberId)
                .map(member -> {
                    // Vérifie que l'utilisateur est admin de la structure du membre
                    boolean isAdminOfCorrectStructure = isStructureAdmin(member.getTeam().getStructure().getId(), currentUser);
                    // Vérifie que l'utilisateur n'est pas en train de modifier son propre rôle
                    boolean isNotSelfManagement = !member.getUser().getId().equals(currentUser.getId());

                    return isAdminOfCorrectStructure && isNotSelfManagement;
                })
                .orElse(false);
    }
}