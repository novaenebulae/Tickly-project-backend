package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import edu.cda.project.ticklybackend.dtos.user.UserSearchResponseDto;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.UserMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.StructureAdministratorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import edu.cda.project.ticklybackend.repositories.mailing.VerificationTokenRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.repositories.ticket.ReservationRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.repositories.user.UserFavoriteStructureRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TokenService;
import edu.cda.project.ticklybackend.services.interfaces.UserService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final UserFavoriteStructureRepository favoriteRepository;
    private final StructureRepository structureRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AuthUtils authUtils;
    private final TokenService tokenService;
    private final MailingService mailingService;
    private final VerificationTokenRepository tokenRepository; // Ajouté pour la suppression en cascade
    private final TicketRepository ticketRepository;
    private final TeamManagementServiceImpl teamService;

    private static final String AVATAR_SUBDIRECTORY = "avatars";
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.userToUserProfileResponseDto(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateUserProfile(Long userId, UserProfileUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (StringUtils.hasText(updateDto.getFirstName())) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (StringUtils.hasText(updateDto.getLastName())) {
            user.setLastName(updateDto.getLastName());
        }
        if (StringUtils.hasText(updateDto.getEmail()) && !Objects.equals(user.getEmail(), updateDto.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new BadRequestException("L'adresse email '" + updateDto.getEmail() + "' est déjà utilisée.");
            }
            user.setEmail(updateDto.getEmail());
        }


        User updatedUser = userRepository.save(user);
        return userMapper.userToUserProfileResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public String updateUserAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Supprimer l'ancien avatar s'il existe
        if (StringUtils.hasText(user.getAvatarPath())) {
            try {
                fileStorageService.deleteFile(user.getAvatarPath(), AVATAR_SUBDIRECTORY);
            } catch (Exception e) {
                // Log l'erreur mais continuer, car l'important est de sauvegarder le nouveau
                System.err.println("Erreur lors de la suppression de l'ancien avatar : " + e.getMessage());
            }
        }

        // Stocker le nouveau fichier
        String newAvatarPath = fileStorageService.storeFile(file, AVATAR_SUBDIRECTORY);
        user.setAvatarPath(newAvatarPath);
        userRepository.save(user);

        return fileStorageService.getFileUrl(newAvatarPath, AVATAR_SUBDIRECTORY);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSearchResponseDto> searchUsers(String query, Pageable pageable) {
        // Implémentation de recherche simple (peut être améliorée avec des Specifications JPA)
        // Pour l'instant, recherche par prénom ou nom contenant la query
        // Note: Ceci n'est pas sensible à la casse par défaut avec toutes les DB,
        // des configurations spécifiques ou des fonctions de DB peuvent être nécessaires pour une recherche insensible à la casse.
        if (!StringUtils.hasText(query)) {
            return userRepository.findAll(pageable).map(userMapper::userToUserSearchResponseDto);
        }
        // Ceci est une simplification. Une vraie recherche utiliserait des prédicats.
        // Pour l'instant, on retourne tous les utilisateurs si la query est vide, sinon une page vide.
        // Une implémentation plus complète nécessiterait une méthode de repository personnalisée.
        // Par exemple : userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, pageable);
        return Page.empty(pageable); // À remplacer par une vraie logique de recherche
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserFavoriteStructureDto> getCurrentUserFavoriteStructures() {
        Long userId = authUtils.getCurrentAuthenticatedUserId();
        List<UserFavoriteStructure> favorites = favoriteRepository.findByUserId(userId);
        // On passe fileStorageService en contexte pour que MapStruct puisse l'utiliser dans les mappers imbriqués
        return userMapper.userFavoriteStructuresToUserFavoriteStructureDtos(favorites, fileStorageService);
    }


    @Override
    @Transactional
    public UserFavoriteStructureDto addFavoriteStructure(Long userId, Long structureId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

        if (favoriteRepository.existsByUserIdAndStructureId(userId, structureId)) {
            throw new BadRequestException("Cette structure est déjà dans vos favoris.");
        }

        UserFavoriteStructure favorite = new UserFavoriteStructure();
        favorite.setUser(user);
        favorite.setStructure(structure);
        UserFavoriteStructure savedFavorite = favoriteRepository.save(favorite);
        return userMapper.userFavoriteStructureToUserFavoriteStructureDto(savedFavorite, fileStorageService);
    }

    @Override
    @Transactional
    public void removeFavoriteStructure(Long userId, Long structureId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        if (!structureRepository.existsById(structureId)) {
            throw new ResourceNotFoundException("Structure", "id", structureId);
        }
        if (!favoriteRepository.existsByUserIdAndStructureId(userId, structureId)) {
            throw new ResourceNotFoundException("FavoriteStructure", "userId/structureId", userId + "/" + structureId);
        }
        favoriteRepository.deleteByUserIdAndStructureId(userId, structureId);
    }

    // Méthodes pour l'utilisateur courant
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getCurrentUserProfile() {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        return getUserProfile(currentUser.getId());
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateCurrentUserProfile(UserProfileUpdateDto updateDto) {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        return updateUserProfile(currentUser.getId(), updateDto);
    }

    @Override
    @Transactional
    public String updateCurrentUserAvatar(MultipartFile file) {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        return updateUserAvatar(currentUser.getId(), file);
    }


    @Override
    @Transactional
    public UserFavoriteStructureDto addCurrentUserFavoriteStructure(Long structureId) {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        return addFavoriteStructure(currentUser.getId(), structureId);
    }

    @Override
    @Transactional
    public void removeCurrentUserFavoriteStructure(Long structureId) {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        removeFavoriteStructure(currentUser.getId(), structureId);
    }

    @Override
    @Transactional
    public void requestAccountDeletion() {
        User currentUser = authUtils.getCurrentAuthenticatedUser();

        // GARDE-FOU: Passation de pouvoir pour un administrateur de structure
        if (currentUser instanceof StructureAdministratorUser admin) {
            Structure managedStructure = admin.getStructure();
            // Vérifie s'il gère une structure qui est toujours active
            if (managedStructure != null && managedStructure.isActive()) {
                long adminCount = teamService.countAdminsForStructure(managedStructure.getId());

                // S'il est le seul admin (ou si le compte est 0 pour une raison quelconque, on est prudent), on bloque.
                if (adminCount <= 1) {
                    throw new BadRequestException("Vous ne pouvez pas supprimer votre compte car vous êtes le seul administrateur de la structure '"
                            + managedStructure.getName() + "'. Avant de supprimer votre compte, vous devez soit :\n" +
                            "1. Promouvoir un autre membre de l'équipe au rôle d'administrateur via l'interface de gestion d'équipe\n" +
                            "2. Supprimer définitivement la structure si elle n'est plus utilisée\n" +
                            "Ces actions peuvent être effectuées depuis le tableau de bord de votre structure.");
                }

            }
        }

        // Si le garde-fou est passé, on continue le processus de demande de suppression
        VerificationToken deletionToken = tokenService.createToken(currentUser, TokenType.ACCOUNT_DELETION_CONFIRMATION, Duration.ofHours(1), null);
        String deletionLink = "/users/confirm-deletion?token=" + deletionToken.getToken();

        mailingService.sendAccountDeletionRequest(currentUser.getEmail(), currentUser.getFirstName(), deletionLink);
    }

    @Override
    @Transactional
    public void confirmAccountDeletion(String tokenString) {
        VerificationToken token = tokenService.validateToken(tokenString, TokenType.ACCOUNT_DELETION_CONFIRMATION);
        User user = token.getUser();

        log.info("Début de l'anonymisation du compte pour l'utilisateur: {}", user.getEmail());

        // Sauvegarde de l'email original pour l'envoi de la confirmation
        String originalEmail = user.getEmail();
        String originalFirstName = user.getFirstName();

        // 1. Anonymiser les informations personnelles de l'utilisateur
        user.setFirstName("Utilisateur");
        user.setLastName("Supprimé");
        user.setEmail(user.getId() + "@deleted.user.tickly"); // Garantit l'unicité
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Invalide le mot de passe
        user.setAvatarPath(null);
        user.setEmailValidated(false); // Invalide l'email

        Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByUserId(user.getId());
        if (teamMemberOpt.isPresent()) {
            TeamMember teamMember = teamMemberOpt.get();
            // Vérifier s'il est le dernier administrateur de la structure
            if (teamMember.getRole() == UserRole.STRUCTURE_ADMINISTRATOR) {
                Long structureId = teamMember.getTeam().getStructure().getId();
                if (teamService.countAdminsForStructure(structureId) <= 1) {
                    throw new BadRequestException("Vous ne pouvez pas supprimer votre compte car vous êtes le dernier administrateur de votre structure. " +
                            "Veuillez d'abord transférer la propriété de la structure à un autre membre ou supprimer la structure.");
                }
            }
            // Supprimer l'entrée de membre d'équipe
            teamMemberRepository.delete(teamMember);
            log.info("L'utilisateur {} a été retiré de son équipe.", user.getEmail());
        }

        // 2. Anonymiser les billets associés
        List<Ticket> ticketsToAnonymize = ticketRepository.findByUserId(user.getId());
        if (ticketsToAnonymize != null && !ticketsToAnonymize.isEmpty()) {
            log.debug("Anonymisation de {} billet(s) pour l'utilisateur ID: {}", ticketsToAnonymize.size(), user.getId());
            for (Ticket ticket : ticketsToAnonymize) {
                // On anonymise le nom du participant sur le billet
                ticket.setParticipantFirstName("Participant");
                ticket.setParticipantLastName("Anonyme");
                ticket.setParticipantEmail("anonyme@tickly.app");
            }
            ticketRepository.saveAll(ticketsToAnonymize);
        }

        // 3. Supprimer les données purement personnelles (favoris, tokens)
        log.debug("Suppression des favoris pour l'utilisateur ID: {}", user.getId());
        favoriteRepository.deleteAllByUser(user);

        log.debug("Suppression des tokens pour l'utilisateur ID: {}", user.getId());
        tokenRepository.deleteAll(tokenRepository.findByUser(user));

        // 4. Mettre à jour l'entité User avec les données anonymisées
        userRepository.save(user);

        // 5. Envoyer l'e-mail de confirmation final à l'adresse e-mail originale
        mailingService.sendAccountDeletionConfirmation(originalEmail, originalFirstName);
        log.info("Compte de l'utilisateur ID {} anonymisé avec succès.", user.getId());
    }
}
