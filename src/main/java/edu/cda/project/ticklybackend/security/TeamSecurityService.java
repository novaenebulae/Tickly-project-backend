package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.team.Team;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Service pour gérer la logique de sécurité spécifique à la gestion d'équipe.
 * Utilisé principalement dans les annotations @PreAuthorize.
 */
@Service("teamSecurityService")
@RequiredArgsConstructor
public class TeamSecurityService {

    // CORRECTION : Dépendance directe au StructureRepository pour une vérification robuste.
    private final StructureRepository structureRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * Vérifie si l'utilisateur authentifié est l'administrateur de la structure donnée.
     * Cette logique ne dépend plus de l'existence d'une entité Team.
     *
     * @param structureId L'ID de la structure à vérifier.
     * @param user        L'utilisateur authentifié.
     * @return true si l'utilisateur est bien l'administrateur de la structure.
     */
    @Transactional(readOnly = true)
    public boolean isStructureAdmin(Long structureId, User user) {
        if (structureId == null || user == null) {
            return false;
        }
        // CORRECTION : La logique vérifie maintenant directement le champ 'administrator'
        // de l'entité Structure, ce qui est correct et résout le problème.
        return structureRepository.findById(structureId)
                .map(Structure::getAdministrator)
                .map(admin -> admin != null && Objects.equals(admin.getId(), user.getId()))
                .orElse(false);
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
     * Nouvelle méthode pour vérifier si un utilisateur est l'admin de l'équipe d'un membre donné.
     * Utile pour sécuriser la mise à jour et la suppression de membres.
     *
     * @param memberId L'ID de l'enregistrement TeamMember.
     * @param user     L'utilisateur authentifié qui effectue l'action.
     * @return true si l'utilisateur est admin de la structure du membre.
     */
    @Transactional(readOnly = true)
    public boolean isTeamAdminOfMember(Long memberId, User user) {
        if (memberId == null || user == null) {
            return false;
        }

        return teamMemberRepository.findById(memberId)
                .map(TeamMember::getTeam)
                .map(Team::getStructure)
                .map(Structure::getAdministrator)
                .map(admin -> admin != null && Objects.equals(admin.getId(), user.getId()))
                .orElse(false);
    }
}