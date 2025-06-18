package edu.cda.project.ticklybackend.mappers.user;


import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserSearchResponseDto;
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

// @Mapper indique à MapStruct de générer une implémentation pour cette interface.
// componentModel = "spring" permet d'injecter ce mapper comme un bean Spring.
@Mapper(componentModel = "spring", imports = {StaffUser.class})
public abstract class UserMapper {

    // Juste pour qu'il reste dans les imports car utilisé dans l'expression du mapping StructureId
    protected StaffUser staffUser;

    @Autowired
    protected FileStorageService fileStorageService;

    // Convertit une entité User en AuthResponseDto
    // Le mapping de 'expiresIn' et 'accessToken' se fera manuellement dans le service
    // car ils ne proviennent pas directement de l'entité User.
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildUserAvatarUrl")
    @Mapping(target = "structureId", expression = "java(user instanceof StaffUser && ((StaffUser) user).getStructure()!= null? ((StaffUser) user).getStructure().getId() : null)")
    public abstract AuthResponseDto userToAuthResponseDto(User user);

    // Pour UserProfileResponseDto (Étape 3)
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildUserAvatarUrl")
    @Mapping(target = "structureId", expression = "java(user instanceof StaffUser && ((StaffUser) user).getStructure()!= null? ((StaffUser) user).getStructure().getId() : null)")
    public abstract UserProfileResponseDto userToUserProfileResponseDto(User user); // ERREUR LIGNES 51 & 88

    // Pour UserSearchResponseDto (Étape 3)
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildUserAvatarUrl")
    public abstract UserSearchResponseDto userToUserSearchResponseDto(User user); // Pour la référence de méthode à la LIGNE 123

    public List<UserSearchResponseDto> usersToUserSearchResponseDtos(List<User> users) {
        if (users == null) return null;
        return users.stream().map(this::userToUserSearchResponseDto).collect(Collectors.toList());
    }

    // Pour UserFavoriteStructureDto (Étape 3)
    @Mapping(target = "userId", source = "user.id")
    // 'structure' sera mappé par StructureMapper via 'uses'
    public abstract UserFavoriteStructureDto userFavoriteStructureToUserFavoriteStructureDto(UserFavoriteStructure favorite, @Context FileStorageService fsService); // ERREUR LIGNE 158

    // Méthode pour la liste des favoris (Étape 3)
    public List<UserFavoriteStructureDto> userFavoriteStructuresToUserFavoriteStructureDtos(List<UserFavoriteStructure> favorites, @Context FileStorageService fsService) { // ERREUR LIGNE 139
        if (favorites == null) {
            return null;
        }
        return favorites.stream()
                .map(fav -> this.userFavoriteStructureToUserFavoriteStructureDto(fav, fsService))
                .collect(Collectors.toList());
    }

    @Named("buildUserAvatarUrl")
    protected String buildUserAvatarUrl(String avatarPath) {
        if (avatarPath == null ||
                avatarPath.isBlank() ||
                fileStorageService == null) {
            return null;
        }
        return fileStorageService.getFileUrl(avatarPath, "avatars");
    }
}