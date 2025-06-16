package edu.cda.project.ticklybackend.services.impl;


import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import edu.cda.project.ticklybackend.dtos.user.UserSearchResponseDto;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.UserMapper;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import edu.cda.project.ticklybackend.repositories.UserFavoriteStructureRepository;
import edu.cda.project.ticklybackend.repositories.UserRepository;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.UserService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final UserFavoriteStructureRepository favoriteRepository;
    private final StructureRepository structureRepository; // Pour vérifier l'existence de la structure
    private final AuthUtils authUtils; // Pour récupérer l'utilisateur courant

    private static final String AVATAR_SUBDIRECTORY = "avatars";

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

        // Gestion du changement de mot de passe
        if (StringUtils.hasText(updateDto.getNewPassword())) {
            if (!StringUtils.hasText(updateDto.getCurrentPassword())) {
                throw new BadRequestException("Le mot de passe actuel est requis pour changer le mot de passe.");
            }
            if (!passwordEncoder.matches(updateDto.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Le mot de passe actuel est incorrect.");
            }
            if (!Objects.equals(updateDto.getNewPassword(), updateDto.getConfirmNewPassword())) {
                throw new BadRequestException("Le nouveau mot de passe et sa confirmation ne correspondent pas.");
            }
            user.setPassword(passwordEncoder.encode(updateDto.getNewPassword()));
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
    public List<UserFavoriteStructureDto> getUserFavoriteStructures(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        List<UserFavoriteStructure> favorites = favoriteRepository.findByUserId(userId);
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
    @Transactional(readOnly = true)
    public List<UserFavoriteStructureDto> getCurrentUserFavoriteStructures() {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        return getUserFavoriteStructures(currentUser.getId());
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
}