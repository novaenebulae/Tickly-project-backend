package edu.cda.project.ticklybackend.mappers.user;

import edu.cda.project.ticklybackend.dtos.friendship.*;
import edu.cda.project.ticklybackend.models.user.Friendship;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FriendshipMapper {

    // --- Mappings pour les listes d'amis et de demandes ---

    @Mapping(target = ".", source = "friendship", qualifiedByName = "mapToFriendResponse")
    FriendResponseDto toFriendResponseDto(Friendship friendship, @Context Long currentUserId, @Context FileStorageService fsService);

    List<FriendResponseDto> toFriendResponseDtoList(List<Friendship> friendships, @Context Long currentUserId, @Context FileStorageService fsService);

    @Mapping(target = "friendshipId", source = "id")
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "requestedAt", source = "createdAt")
    ReceivedFriendRequestResponseDto toReceivedFriendRequestDto(Friendship friendship, @Context FileStorageService fsService);

    List<ReceivedFriendRequestResponseDto> toReceivedFriendRequestDtoList(List<Friendship> friendships, @Context FileStorageService fsService);

    @Mapping(target = "friendshipId", source = "id")
    @Mapping(target = "receiver", source = "receiver")
    @Mapping(target = "sentAt", source = "createdAt")
    SentFriendRequestResponseDto toSentFriendRequestDto(Friendship friendship, @Context FileStorageService fsService);

    List<SentFriendRequestResponseDto> toSentFriendRequestDtoList(List<Friendship> friendships, @Context FileStorageService fsService);

    // Méthode qualifiée pour déterminer qui est "l'autre" dans la relation d'amitié
    @Named("mapToFriendResponse")
    default FriendResponseDto mapFriendshipToFriendDto(Friendship friendship, @Context Long currentUserId, @Context FileStorageService fsService) {
        if (friendship == null) {
            return null;
        }

        User friendUser = friendship.getSender().getId().equals(currentUserId) ? friendship.getReceiver() : friendship.getSender();
        String avatarUrl = (friendUser.getAvatarPath() != null && fsService != null) ? fsService.getFileUrl(friendUser.getAvatarPath(), "avatars") : null;

        return new FriendResponseDto(
                friendUser.getId(),
                friendUser.getFirstName(),
                friendUser.getLastName(),
                avatarUrl,
                friendship.getId(),
                friendship.getUpdatedAt() // "since" est la date de la dernière mise à jour (l'acceptation)
        );
    }
}
