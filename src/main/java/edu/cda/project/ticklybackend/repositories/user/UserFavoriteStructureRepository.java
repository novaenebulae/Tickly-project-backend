package edu.cda.project.ticklybackend.repositories.user;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteStructureRepository extends JpaRepository<UserFavoriteStructure, Long> {

    List<UserFavoriteStructure> findByUserId(Long userId);

    Optional<UserFavoriteStructure> findByUserIdAndStructureId(Long userId, Long structureId);

    boolean existsByUserIdAndStructureId(Long userId, Long structureId);

    void deleteByUserIdAndStructureId(Long userId, Long structureId);

    void deleteAllByUser(User user);
}