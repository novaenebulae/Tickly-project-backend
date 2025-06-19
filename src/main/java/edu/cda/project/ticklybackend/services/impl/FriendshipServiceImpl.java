package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.friendship.FriendsDataResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SendFriendRequestDto;
import edu.cda.project.ticklybackend.dtos.friendship.UpdateFriendshipStatusDto;
import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.FriendshipMapper;
import edu.cda.project.ticklybackend.models.user.Friendship;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.user.FriendshipRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.FriendshipService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipMapper friendshipMapper;
    private final FileStorageService fileStorageService;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    public FriendsDataResponseDto getFriendsData() {
        Long currentUserId = authUtils.getCurrentAuthenticatedUserId();

        List<Friendship> acceptedFriendships = friendshipRepository.findAcceptedFriends(currentUserId);
        List<Friendship> pendingRequests = friendshipRepository.findByReceiverIdAndStatus(currentUserId, FriendshipStatus.PENDING);
        List<Friendship> sentRequests = friendshipRepository.findBySenderIdAndStatus(currentUserId, FriendshipStatus.PENDING);

        return new FriendsDataResponseDto(
                friendshipMapper.toFriendResponseDtoList(acceptedFriendships, currentUserId, fileStorageService),
                friendshipMapper.toReceivedFriendRequestDtoList(pendingRequests, fileStorageService),
                friendshipMapper.toSentFriendRequestDtoList(sentRequests, fileStorageService)
        );
    }

    @Override
    public void sendFriendRequest(SendFriendRequestDto requestDto) {
        Long senderId = authUtils.getCurrentAuthenticatedUserId();
        Long receiverId = requestDto.getReceiverId();

        if (Objects.equals(senderId, receiverId)) {
            throw new BadRequestException("Vous ne pouvez pas vous ajouter vous-même comme ami.");
        }

        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", receiverId));

        friendshipRepository.findFriendshipBetweenUsers(senderId, receiverId).ifPresent(f -> {
            throw new BadRequestException("Une relation d'amitié existe déjà ou est en attente avec cet utilisateur.");
        });

        Friendship newRequest = new Friendship();
        newRequest.setSender(sender);
        newRequest.setReceiver(receiver);
        newRequest.setStatus(FriendshipStatus.PENDING);

        friendshipRepository.save(newRequest);
    }

    @Override
    public void updateFriendshipStatus(Long friendshipId, UpdateFriendshipStatusDto updateDto) {
        Long currentUserId = authUtils.getCurrentAuthenticatedUserId();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", friendshipId));

        FriendshipStatus newStatus = updateDto.getStatus();

        // Logique de validation des permissions
        switch (newStatus) {
            case ACCEPTED, REJECTED:
                if (!friendship.getReceiver().getId().equals(currentUserId)) {
                    throw new InvalidTokenException("Seul le destinataire peut accepter ou refuser une demande.");
                }
                break;
            case CANCELLED_BY_SENDER:
                if (!friendship.getSender().getId().equals(currentUserId)) {
                    throw new InvalidTokenException("Seul l'émetteur peut annuler sa demande.");
                }
                break;
            default:
                throw new BadRequestException("Le statut fourni n'est pas valide pour cette action.");
        }

        friendship.setStatus(newStatus);
        friendshipRepository.save(friendship);
    }

    @Override
    public void removeFriend(Long friendUserId) {
        Long currentUserId = authUtils.getCurrentAuthenticatedUserId();
        Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "friendId", friendUserId));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new BadRequestException("Aucune amitié active à supprimer avec cet utilisateur.");
        }

        friendshipRepository.delete(friendship);
    }
}