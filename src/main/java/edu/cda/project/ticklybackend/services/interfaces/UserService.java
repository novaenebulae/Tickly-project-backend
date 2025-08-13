package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserProfileResponseDto getUserProfile(Long userId);

    UserProfileResponseDto updateUserProfile(Long userId, UserProfileUpdateDto updateDto);

    String updateUserAvatar(Long userId, MultipartFile file);

    UserFavoriteStructureDto addFavoriteStructure(Long userId, Long structureId);

    void removeFavoriteStructure(Long userId, Long structureId);

    UserProfileResponseDto getCurrentUserProfile(); // Pour /users/me

    UserProfileResponseDto updateCurrentUserProfile(UserProfileUpdateDto updateDto); // Pour /users/me

    String updateCurrentUserAvatar(MultipartFile file); // Pour /users/me/avatar

    List<UserFavoriteStructureDto> getCurrentUserFavoriteStructures(); // Pour /users/me/favorites/structures

    UserFavoriteStructureDto addCurrentUserFavoriteStructure(Long structureId); // Pour /users/me/favorites/structures

    void removeCurrentUserFavoriteStructure(Long structureId); // Pour /users/me/favorites/structures/{structureId}

    void requestAccountDeletion();

    void confirmAccountDeletion(String tokenString);
}