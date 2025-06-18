package edu.cda.project.ticklybackend.repositories.user;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteStructureRepository extends JpaRepository<UserFavoriteStructure, Long> {

    List<UserFavoriteStructure> findByUserId(Long userId);

    Optional<UserFavoriteStructure> findByUserIdAndStructureId(Long userId, Long structureId);

    boolean existsByUserIdAndStructureId(Long userId, Long structureId);

    void deleteByUserIdAndStructureId(Long userId, Long structureId);

    void deleteAllByUser(User user);

    /**
     * Supprime toutes les entrées de favoris liées à un ID de structure spécifique.
     * C'est utile lors de la suppression d'une structure pour nettoyer les relations.
     *
     * @param structureId L'ID de la structure dont les favoris doivent être supprimés.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM UserFavoriteStructure ufs WHERE ufs.structure.id = ?1")
    void deleteByStructureId(Long structureId);
}