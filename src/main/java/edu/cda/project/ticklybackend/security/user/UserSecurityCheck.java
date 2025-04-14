package edu.cda.project.ticklybackend.security.user;

import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
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

import java.util.Objects;

@Aspect
@Component
public class UserSecurityCheck {
    @Autowired
    private UserService userService;

    @Before("@annotation(isUser) && args(id,..)")
    public void checkUser(JoinPoint joinPoint, IsUser isUser, Integer id) {
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

        if (!user.getId().equals(id)) {
            throw new AccessDeniedException("Accès refusé. Type d'utilisateur incorrect.");
        }
    }
}
