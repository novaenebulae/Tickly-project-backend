package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Service pour gérer la logique de sécurité spécifique aux structures.
 * Utilisé principalement dans les annotations @PreAuthorize.
 */
@Service("structureSecurityService") // Nom du bean à utiliser dans SpEL
@RequiredArgsConstructor
public class StructureSecurityService {

    private final StructureRepository structureRepository;

    /**
     * Vérifie si l'utilisateur spécifié est l'administrateur de la structure donnée.
     *
     * @param structureId L'ID de la structure.
     * @param userId      L'ID de l'utilisateur.
     * @return true si l'utilisateur est l'administrateur de la structure, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long structureId, Long userId) {
        if (structureId == null ||
                userId == null) {
            return false;
        }
        return structureRepository.findById(structureId)
                .map(Structure::getAdministrator)
                .map(User::getId)
                .map(adminId -> Objects.equals(adminId, userId))
                .orElse(false);
    }


    // TODO : implémenter la vérification dans le repo.

    /**
     * Vérifie si l'utilisateur spécifié est l'administrateur de la structure à laquelle appartient la zone (Area).
     *
     * @param areaId L'ID de la StructureArea.
     * @param userId L'ID de l'utilisateur.
     * @return true si l'utilisateur est l'administrateur de la structure parente, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isAreaOwnerViaStructure(Long areaId, Long userId) {
        // Cette méthode nécessiterait que StructureAreaRepository ait une méthode pour remonter à la structure
        // ou que StructureArea ait une référence directe à l'ID de l'administrateur de sa structure.
        // Pour simplifier, on suppose que la vérification se fait sur la structureId passée en paramètre au contrôleur.
        // Si une vérification plus profonde est nécessaire, il faudrait enrichir les entités/repositories.
        // Par exemple, si le contrôleur a déjà la structureId:
        // @PreAuthorize("... @structureSecurityService.isOwner(#structureId, authentication.principal.id)")
        // Cette méthode est un placeholder pour une logique plus complexe si nécessaire.
        return false; // À implémenter si une vérification directe sur areaId est requise sans structureId.
    }
}