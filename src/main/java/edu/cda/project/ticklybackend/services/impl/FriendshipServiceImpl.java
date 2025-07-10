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
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipMapper friendshipMapper;
    private final FileStorageService fileStorageService;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    public FriendsDataResponseDto getFriendsData() {
        LoggingUtils.logMethodEntry(log, "getFriendsData");

        try {
            log.debug("Récupération des données d'amitié pour l'utilisateur courant");
            Long currentUserId = authUtils.getCurrentAuthenticatedUserId();
            LoggingUtils.setUserId(currentUserId);
            log.debug("ID de l'utilisateur courant: {}", currentUserId);

            List<Friendship> acceptedFriendships = friendshipRepository.findAcceptedFriends(currentUserId);
            List<Friendship> pendingRequests = friendshipRepository.findByReceiverIdAndStatus(currentUserId, FriendshipStatus.PENDING);
            List<Friendship> sentRequests = friendshipRepository.findBySenderIdAndStatus(currentUserId, FriendshipStatus.PENDING);

            log.debug("Données récupérées: {} amis acceptés, {} demandes reçues, {} demandes envoyées", 
                    acceptedFriendships.size(), pendingRequests.size(), sentRequests.size());

            FriendsDataResponseDto responseDto = new FriendsDataResponseDto(
                    friendshipMapper.toFriendResponseDtoList(acceptedFriendships, currentUserId, fileStorageService),
                    friendshipMapper.toReceivedFriendRequestDtoList(pendingRequests, fileStorageService),
                    friendshipMapper.toSentFriendRequestDtoList(sentRequests, fileStorageService)
            );

            log.debug("Réponse DTO créée avec succès");

            LoggingUtils.logMethodExit(log, "getFriendsData", responseDto);
            return responseDto;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des données d'amitié", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void sendFriendRequest(SendFriendRequestDto requestDto) {
        LoggingUtils.logMethodEntry(log, "sendFriendRequest", "requestDto", requestDto);

        try {
            log.debug("Début de l'envoi d'une demande d'ami à l'email: {}", requestDto.getEmail());
            Long senderId = authUtils.getCurrentAuthenticatedUserId();
            LoggingUtils.setUserId(senderId);
            log.debug("ID de l'expéditeur: {}", senderId);

            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> {
                        LoggingUtils.logException(log, "Utilisateur expéditeur non trouvé avec ID: " + senderId, null);
                        return new ResourceNotFoundException("User", "id", senderId);
                    });

            if (sender.getEmail().equalsIgnoreCase(requestDto.getEmail())) {
                log.warn("Tentative d'ajout de soi-même comme ami par l'utilisateur ID: {}", senderId);
                throw new BadRequestException("Vous ne pouvez pas vous ajouter vous-même comme ami.");
            }

            log.debug("Recherche du destinataire avec l'email: {}", requestDto.getEmail());
            User receiver = userRepository.findByEmail(requestDto.getEmail())
                    .orElseThrow(() -> {
                        LoggingUtils.logException(log, "Destinataire non trouvé avec l'email: " + requestDto.getEmail(), null);
                        return new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + requestDto.getEmail());
                    });
            log.debug("Destinataire trouvé avec ID: {}", receiver.getId());

            friendshipRepository.findFriendshipBetweenUsers(senderId, receiver.getId()).ifPresent(f -> {
                log.warn("Relation d'amitié existante ou en attente entre les utilisateurs ID: {} et {}", senderId, receiver.getId());
                throw new BadRequestException("Une relation d'amitié existe déjà ou est en attente avec cet utilisateur.");
            });

            log.debug("Création d'une nouvelle demande d'ami entre les utilisateurs ID: {} et {}", senderId, receiver.getId());
            Friendship newRequest = new Friendship();
            newRequest.setSender(sender);
            newRequest.setReceiver(receiver);
            newRequest.setStatus(FriendshipStatus.PENDING);

            friendshipRepository.save(newRequest);
            log.info("Demande d'ami envoyée avec succès de {} à {}", sender.getEmail(), receiver.getEmail());

            LoggingUtils.logMethodExit(log, "sendFriendRequest");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'envoi d'une demande d'ami", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }


    @Override
    public void updateFriendshipStatus(Long friendshipId, UpdateFriendshipStatusDto updateDto) {
        LoggingUtils.logMethodEntry(log, "updateFriendshipStatus", "friendshipId", friendshipId, "updateDto", updateDto);

        try {
            log.debug("Début de la mise à jour du statut de la demande d'ami ID: {} vers le statut: {}", friendshipId, updateDto.getStatus());
            Long currentUserId = authUtils.getCurrentAuthenticatedUserId();
            LoggingUtils.setUserId(currentUserId);
            log.debug("ID de l'utilisateur courant: {}", currentUserId);

            Friendship friendship = friendshipRepository.findById(friendshipId)
                    .orElseThrow(() -> {
                        LoggingUtils.logException(log, "Demande d'ami non trouvée avec ID: " + friendshipId, null);
                        return new ResourceNotFoundException("Friendship", "id", friendshipId);
                    });
            log.debug("Demande d'ami trouvée: expéditeur ID={}, destinataire ID={}, statut actuel={}", 
                    friendship.getSender().getId(), friendship.getReceiver().getId(), friendship.getStatus());

            FriendshipStatus newStatus = updateDto.getStatus();

            // Logique de validation des permissions
            log.debug("Vérification des permissions pour la mise à jour du statut");
            switch (newStatus) {
                case ACCEPTED, REJECTED:
                    if (!friendship.getReceiver().getId().equals(currentUserId)) {
                        log.warn("Tentative non autorisée de mise à jour du statut: l'utilisateur ID: {} n'est pas le destinataire", currentUserId);
                        throw new InvalidTokenException("Seul le destinataire peut accepter ou refuser une demande.");
                    }
                    log.debug("Permission validée pour accepter/refuser la demande");
                    break;
                case CANCELLED_BY_SENDER:
                    if (!friendship.getSender().getId().equals(currentUserId)) {
                        log.warn("Tentative non autorisée de mise à jour du statut: l'utilisateur ID: {} n'est pas l'expéditeur", currentUserId);
                        throw new InvalidTokenException("Seul l'émetteur peut annuler sa demande.");
                    }
                    log.debug("Permission validée pour annuler la demande");
                    break;
                default:
                    log.warn("Statut invalide fourni: {}", newStatus);
                    throw new BadRequestException("Le statut fourni n'est pas valide pour cette action.");
            }

            if (newStatus == FriendshipStatus.CANCELLED_BY_SENDER) {
                log.debug("Suppression de la demande d'ami ID: {}", friendshipId);
                friendshipRepository.delete(friendship);
                log.info("Demande d'ami ID: {} annulée et supprimée", friendshipId);
            } else {
                log.debug("Mise à jour du statut de la demande d'ami ID: {} de {} à {}", 
                        friendshipId, friendship.getStatus(), newStatus);
                friendship.setStatus(newStatus);
                friendshipRepository.save(friendship);
                log.info("Statut de la demande d'ami ID: {} mis à jour avec succès vers: {}", friendshipId, newStatus);
            }

            LoggingUtils.logMethodExit(log, "updateFriendshipStatus");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la mise à jour du statut de la demande d'ami", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public void removeFriend(Long friendUserId) {
        LoggingUtils.logMethodEntry(log, "removeFriend", "friendUserId", friendUserId);

        try {
            log.debug("Début de la suppression de l'ami avec l'ID utilisateur: {}", friendUserId);
            Long currentUserId = authUtils.getCurrentAuthenticatedUserId();
            LoggingUtils.setUserId(currentUserId);
            log.debug("ID de l'utilisateur courant: {}", currentUserId);

            Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)
                    .orElseThrow(() -> {
                        LoggingUtils.logException(log, "Relation d'amitié non trouvée entre les utilisateurs ID: " + currentUserId + " et " + friendUserId, null);
                        return new ResourceNotFoundException("Friendship", "friendId", friendUserId);
                    });
            log.debug("Relation d'amitié trouvée: ID={}, statut={}", friendship.getId(), friendship.getStatus());

            if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
                log.warn("Tentative de suppression d'une relation d'amitié non active entre les utilisateurs ID: {} et {}", 
                        currentUserId, friendUserId);
                throw new BadRequestException("Aucune amitié active à supprimer avec cet utilisateur.");
            }

            log.debug("Suppression de la relation d'amitié ID: {}", friendship.getId());
            friendshipRepository.delete(friendship);
            log.info("Relation d'amitié supprimée avec succès entre les utilisateurs ID: {} et {}", 
                    currentUserId, friendUserId);

            LoggingUtils.logMethodExit(log, "removeFriend");
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression d'un ami", e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }
}
