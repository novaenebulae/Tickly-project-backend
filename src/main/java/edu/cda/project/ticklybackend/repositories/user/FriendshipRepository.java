package edu.cda.project.ticklybackend.repositories.user;

import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import edu.cda.project.ticklybackend.models.user.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Trouve les demandes reçues par un utilisateur
    List<Friendship> findByReceiverIdAndStatus(Long receiverId, FriendshipStatus status);

    // Trouve les demandes envoyées par un utilisateur
    List<Friendship> findBySenderIdAndStatus(Long senderId, FriendshipStatus status);

    // Trouve tous les amis (ceux où la demande a été acceptée)
    @Query("SELECT f FROM Friendship f WHERE (f.sender.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriends(Long userId);

    // Trouve une relation d'amitié existante entre deux utilisateurs, quel que soit l'émetteur/destinataire
    @Query("SELECT f FROM Friendship f WHERE (f.sender.id = :userId1 AND f.receiver.id = :userId2) OR (f.sender.id = :userId2 AND f.receiver.id = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(Long userId1, Long userId2);
}