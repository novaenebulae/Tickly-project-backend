package edu.cda.project.ticklybackend.repositories.user;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserFavoriteStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserFavoriteStructureRepository extends JpaRepository<UserFavoriteStructure, Long> {

    List<UserFavoriteStructure> findByUserId(Long userId);

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
    void deleteByStructure_Id(Long structureId);

}