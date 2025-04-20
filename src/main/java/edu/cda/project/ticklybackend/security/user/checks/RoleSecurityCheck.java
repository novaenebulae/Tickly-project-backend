package edu.cda.project.ticklybackend.security.user.checks;

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import edu.cda.project.ticklybackend.security.SecurityConfiguration;
import edu.cda.project.ticklybackend.security.user.annotations.IsPendingStructureAdministrator;
import edu.cda.project.ticklybackend.security.user.annotations.IsSpectator;
import edu.cda.project.ticklybackend.security.user.annotations.IsStructureAdministrator;
import edu.cda.project.ticklybackend.services.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleSecurityCheck {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SecurityConfiguration securityConfiguration; // Ajoutez cette ligne

    public User getUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Non authentifié");
        }

        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        if (user == null) {
            throw new AccessDeniedException("Utilisateur non trouvé");
        }

        return user;
    }

    @Before("@annotation(isStructureAdministrator)")
    public void checkStructureAdministratorAccess(IsStructureAdministrator isStructureAdministrator) {

        User user = getUserContext();

        if (!userService.hasRole(user, UserRole.STRUCTURE_ADMINISTRATOR)) {
            throw new AccessDeniedException("Access denied. Structure Administrator role required.");
        }
    }

    @Before("@annotation(isPendingStructureAdministrator)")
    public void checkPendingStructureAdministratorAccess(IsPendingStructureAdministrator isPendingStructureAdministrator) {

        StaffUser user = (StaffUser) getUserContext();

        if (!userService.hasRole(user, UserRole.STRUCTURE_ADMINISTRATOR) && user.getStructure() != null) {
            throw new AccessDeniedException("Access denied. Structure Administrator role required.");
        }
    }

    @Before("@annotation(isSpectator)")
    public void checkStructureAdministratorAccess(IsSpectator isSpectator) {

        User user = getUserContext();

        if (!userService.hasRole(user, UserRole.SPECTATOR)) {
            throw new AccessDeniedException("Access denied. Spectator role required.");
        }
    }
}
