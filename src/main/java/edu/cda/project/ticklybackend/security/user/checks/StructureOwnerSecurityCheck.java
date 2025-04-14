package edu.cda.project.ticklybackend.security.user.checks;

import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import edu.cda.project.ticklybackend.security.user.annotations.IsStructureOwner;
import edu.cda.project.ticklybackend.services.StructureService;
import edu.cda.project.ticklybackend.services.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StructureOwnerSecurityCheck {
    @Autowired
    private UserService userService;
    @Autowired
    private StructureService structureService;

    @Before("@annotation(isStructureOwner) && args(id,..)")
    public void checkStructureOwnerAccess(JoinPoint joinPoint, IsStructureOwner isStructureOwner, Integer id) {
        // Récupérer l'utilisateur actuel
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Non authentifié");
        }

        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        if (user == null) {
            throw new AccessDeniedException("Utilisateur non trouvé");
        }

        // Vérifier si l'utilisateur est un administrateur de structure
        if (!userService.hasRole(user, UserRole.STRUCTURE_ADMINISTRATOR)) {
            throw new AccessDeniedException("Accès refusé. Rôle d'administrateur de structure requis.");
        }

        // Vérifier si l'utilisateur est lié à la structure
        if (user instanceof StructureAdministratorUser) {
            StructureAdministratorUser adminUser = (StructureAdministratorUser) user;
            Structure userStructure = adminUser.getStructure();

            // Récupérer la structure à supprimer
            Structure targetStructure = structureService.findStructureById(id);

            if (targetStructure == null) {
                throw new AccessDeniedException("Structure non trouvée");
            }

            // Vérifier si l'utilisateur est administrateur de cette structure
            if (userStructure == null || !userStructure.getId().equals(targetStructure.getId())) {
                throw new AccessDeniedException("Accès refusé. Vous n'êtes pas administrateur de cette structure.");
            }
        } else {
            throw new AccessDeniedException("Accès refusé. Type d'utilisateur incorrect.");
        }
    }
}
