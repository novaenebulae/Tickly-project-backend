package edu.cda.project.ticklybackend.services; // Assurez-vous que ce package existe ou adaptez

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.dtos.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.UserRegistrationDto;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import edu.cda.project.ticklybackend.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Autowired
    public AuthService(UserDao userDao, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UserService userService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Transactional // Garantit l'atomicité de l'opération (vérification + sauvegarde)
    public AuthResponseDto registerAndLogin(UserRegistrationDto userDto) {
        logger.info("Attempting registration for email: {}", userDto.getEmail());

        // 1. Vérifier si l'email existe déjà
        if (userDao.findUserByEmail(userDto.getEmail()) != null) {
            logger.warn("Registration failed: Email {} already exists.", userDto.getEmail());
            throw new edu.cda.project.ticklybackend.exception.EmailAlreadyExistsException("L'adresse email '" + userDto.getEmail() + "' est déjà utilisée.");
        }

        // 2. Préparer le nouvel utilisateur selon le flag createStructure
        User newUser;
        boolean needsSetup = userDto.isCreateStructure();

        if (needsSetup) {
            logger.debug("Creating StructureAdministratorUser for {}", userDto.getEmail());
            StructureAdministratorUser adminUser = new StructureAdministratorUser();
            adminUser.setEmail(userDto.getEmail());
            adminUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            adminUser.setFirstName(userDto.getFirstName());
            adminUser.setLastName(userDto.getLastName());
            // Le rôle est implicitement STRUCTURE_ADMINISTRATOR via @DiscriminatorValue
            adminUser.setRole(UserRole.STRUCTURE_ADMINISTRATOR);
            adminUser.setStructure(null);
            newUser = adminUser;
        } else {
            logger.debug("Creating SpectatorUser for {}", userDto.getEmail());
            SpectatorUser spectatorUser = new SpectatorUser();
            spectatorUser.setEmail(userDto.getEmail());
            spectatorUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            spectatorUser.setFirstName(userDto.getFirstName());
            spectatorUser.setLastName(userDto.getLastName());
            // Le rôle est implicitement SPECTATOR via @DiscriminatorValue
            spectatorUser.setRole(UserRole.SPECTATOR);
            newUser = spectatorUser;
        }

        // 3. Sauvegarder l'utilisateur via UserService
        User savedUser = userService.saveUser(newUser);
        logger.info("User saved successfully with ID: {}", savedUser.getId());

        // 4. Générer le JWT
        // La méthode generateJwtToken de JwtUtils gère l'ajout conditionnel de needsStructureSetup
        String jwtToken = jwtUtils.generateJwtToken(savedUser);
        logger.debug("JWT generated for user ID {}", savedUser.getId());

        // 5. Retourner la réponse DTO complète
        return new AuthResponseDto(
                jwtToken,
                savedUser.getId(),
                savedUser.getRole().name(),
                needsSetup
        );
    }
}
