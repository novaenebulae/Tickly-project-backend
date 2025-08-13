package edu.cda.project.ticklybackend.mappers.user;


import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserSearchResponseDto;
import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.mappers.structure.StructureMapper;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

// @Mapper indique à MapStruct de générer une implémentation pour cette interface.
// componentModel = "spring" permet d'injecter ce mapper comme un bean Spring.
@Mapper(componentModel = "spring", uses = {StructureMapper.class})
public abstract class UserMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Autowired
    protected TeamMemberRepository teamMemberRepository;

    // Convertit une entité User en AuthResponseDto
    // Le mapping de 'expiresIn' et 'accessToken' se fera manuellement dans le service
    // car ils ne proviennent pas directement de l'entité User.
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildUserAvatarUrl")
    public abstract AuthResponseDto userToAuthResponseDto(User user);

    // Pour UserProfileResponseDto (Étape 3)
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildUserAvatarUrl")
    @Mapping(target = "structureId", expression = "java(resolveStructureId(user))")
    @Mapping(target = "role", expression = "java(resolveRole(user))")
    public abstract UserProfileResponseDto userToUserProfileResponseDto(User user);

    // Pour UserSearchResponseDto (Étape 3)
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildUserAvatarUrl")
    public abstract UserSearchResponseDto userToUserSearchResponseDto(User user);

    public List<UserSearchResponseDto> usersToUserSearchResponseDtos(List<User> users) {
        if (users == null) return null;
        return users.stream().map(this::userToUserSearchResponseDto).collect(Collectors.toList());
    }

    // Pour UserFavoriteStructureDto (Étape 3)
    @Mapping(target = "userId", source = "user.id")
    // 'structure' sera mappé par StructureMapper via 'uses'
    public abstract UserFavoriteStructureDto userFavoriteStructureToUserFavoriteStructureDto(UserFavoriteStructure favorite, @Context FileStorageService fsService);

    // Méthode pour la liste des favoris (Étape 3)
    public List<UserFavoriteStructureDto> userFavoriteStructuresToUserFavoriteStructureDtos(List<UserFavoriteStructure> favorites, @Context FileStorageService fsService) {
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

    /**
     * Convertit un Instant (depuis l'entité) en ZonedDateTime (pour les DTOs), en forçant le fuseau UTC (Z).
     */
    protected ZonedDateTime toZonedDateTime(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }


    protected Long resolveStructureId(User user) {
        return teamMemberRepository
                .findFirstByUserIdAndStatusOrderByJoinedAtDesc(user.getId(), TeamMemberStatus.ACTIVE)
                .map(tm -> tm.getStructure().getId())
                .orElseGet(() -> teamMemberRepository.findByUserId(user.getId())
                        .map(tm -> tm.getStructure().getId())
                        .orElse(null));
    }

    protected UserRole resolveRole(User user) {
        return teamMemberRepository
                .findFirstByUserIdAndStatusOrderByJoinedAtDesc(user.getId(), TeamMemberStatus.ACTIVE)
                .map(TeamMember::getRole)
                .orElseGet(() -> teamMemberRepository.findByUserId(user.getId())
                        .map(TeamMember::getRole)
                        .orElse(null));
    }
}

