package edu.cda.project.ticklybackend.mappers.user;

import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.ReceivedFriendRequestResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SentFriendRequestResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import edu.cda.project.ticklybackend.models.user.Friendship;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FriendshipMapper {

    // --- Mappings pour les listes d'amis et de demandes ---

    @Mapping(target = "friendshipId", source = "id")
    @Mapping(target = "since", source = "updatedAt", qualifiedByName = "friendshipToZonedDateTime")
    @Mapping(target = "friend", source = "friendship", qualifiedByName = "mapFriendshipToFriendSummaryDto")
    FriendResponseDto toFriendResponseDto(Friendship friendship, @Context Long currentUserId, @Context FileStorageService fsService);

    List<FriendResponseDto> toFriendResponseDtoList(List<Friendship> friendships, @Context Long currentUserId, @Context FileStorageService fsService);

    @Mapping(target = "friendshipId", source = "id")
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "requestedAt", source = "createdAt", qualifiedByName = "friendshipToZonedDateTime")
    ReceivedFriendRequestResponseDto toReceivedFriendRequestDto(Friendship friendship, @Context FileStorageService fsService);

    List<ReceivedFriendRequestResponseDto> toReceivedFriendRequestDtoList(List<Friendship> friendships, @Context FileStorageService fsService);

    @Mapping(target = "friendshipId", source = "id")
    @Mapping(target = "receiver", source = "receiver")
    @Mapping(target = "sentAt", source = "createdAt", qualifiedByName = "friendshipToZonedDateTime")
    SentFriendRequestResponseDto toSentFriendRequestDto(Friendship friendship, @Context FileStorageService fsService);

    List<SentFriendRequestResponseDto> toSentFriendRequestDtoList(List<Friendship> friendships, @Context FileStorageService fsService);

    /**
     * Méthode qualifiée qui gère la logique complète pour éviter les problèmes de contexte :
     * 1. Détermine qui est l'ami dans la relation.
     * 2. Mappe cet utilisateur en UserSummaryDto directement.
     */
    @Named("mapFriendshipToFriendSummaryDto")
    @SuppressWarnings("unused") // L'IDE peut le marquer comme non utilisé, mais MapStruct l'utilise.
    default UserSummaryDto mapFriendshipToFriendSummaryDto(Friendship friendship, @Context Long currentUserId, @Context FileStorageService fsService) {
        if (friendship == null) {
            return null;
        }

        User friendUser = friendship.getSender().getId().equals(currentUserId)
                ? friendship.getReceiver()
                : friendship.getSender();

        // Logique de mapping copiée depuis le UserMapper pour être explicite
        String avatarUrl = (friendUser.getAvatarPath() != null && fsService != null)
                ? fsService.getFileUrl(friendUser.getAvatarPath(), "avatars")
                : null;

        return new UserSummaryDto(
                friendUser.getId(),
                friendUser.getFirstName(),
                friendUser.getLastName(),
                avatarUrl
        );
    }

    /**
     * Convertit un Instant (depuis l'entité) en ZonedDateTime (pour les DTOs), en forçant le fuseau UTC (Z).
     */
    @Named("friendshipToZonedDateTime")
    default ZonedDateTime toZonedDateTime(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
