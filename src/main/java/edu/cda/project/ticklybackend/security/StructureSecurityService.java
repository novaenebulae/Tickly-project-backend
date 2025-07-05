package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour gérer la logique de sécurité spécifique aux structures.
 * Utilisé principalement dans les annotations @PreAuthorize.
 */
@Service("structureSecurityService") // Nom du bean à utiliser dans SpEL
@RequiredArgsConstructor
public class StructureSecurityService {

    private final StructureRepository structureRepository;
    private final UserRepository userRepository;

    /**
     * Vérifie si l'utilisateur est un administrateur de la structure donnée.
     * La nouvelle logique se base sur le rôle de l'utilisateur et son ID de structure associé.
     *
     * @param structureId L'ID de la structure.
     * @param userId      L'ID de l'utilisateur.
     * @return true si l'utilisateur est un administrateur de la structure, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long structureId, Long userId) {
        if (structureId == null || userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> {
                    // Vérifie 3 conditions :
                    // 1. L'utilisateur a-t-il un ID de structure ?
                    // 2. Cet ID de structure correspond-il à celui demandé ?
                    // 3. L'utilisateur a-t-il le rôle d'administrateur de structure ?
                    return user.getStructure() != null &&
                            user.getStructure().getId().equals(structureId) &&
                            user.getRole() == UserRole.STRUCTURE_ADMINISTRATOR;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isStructureStaff(Long structureId, Authentication authentication) {
        if (structureId == null || authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return false;
        }

        User user = (User) authentication.getPrincipal();

        return user.getStructure() != null &&
                user.getStructure().getId().equals(structureId) &&
                user.getRole() != UserRole.SPECTATOR;
    }

    /**
     * Nouvelle méthode pour vérifier si un utilisateur a le droit de créer une structure.
     *
     * @param authentication L'objet d'authentification de Spring Security.
     * @return true si l'utilisateur peut créer une structure, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean canCreateStructure(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }

        User user = (User) authentication.getPrincipal();

        // Vérifie si l'utilisateur a le rôle 'ROLE_SPECTATOR', que son email est validé
        // et qu'il n'est pas déjà lié à une structure.
        boolean hasSpectatorRole = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SPECTATOR"));

        // Assurez-vous que votre entité User a les getters `isEmailValidated()` et `getStructureId()`
        return hasSpectatorRole && user.isEmailValidated();
    }

}