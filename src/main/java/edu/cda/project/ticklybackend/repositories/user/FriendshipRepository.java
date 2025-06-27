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

    @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.sender " +
            "JOIN FETCH f.receiver " +
            "WHERE (f.sender.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriends(Long userId);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.sender WHERE f.receiver.id = :userId AND f.status = :status")
    List<Friendship> findByReceiverIdAndStatus(Long userId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.receiver WHERE f.sender.id = :userId AND f.status = :status")
    List<Friendship> findBySenderIdAndStatus(Long userId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.sender.id = :userId1 AND f.receiver.id = :userId2) OR (f.sender.id = :userId2 AND f.receiver.id = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(Long userId1, Long userId2);

}