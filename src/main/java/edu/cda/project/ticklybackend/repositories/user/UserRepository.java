package edu.cda.project.ticklybackend.repositories.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    // Trouve un utilisateur par son email
    Optional<User> findByEmail(String email);

    // Vérifie si un utilisateur existe avec cet email
    boolean existsByEmail(String email);

    /**
     * Recherche tous les utilisateurs associés à une structure spécifique avec un rôle donné.
     * Utilisé pour trouver les administrateurs d'une structure avant sa suppression.
     *
     * @param structureId L'ID de la structure.
     * @param role        Le rôle de l'utilisateur.
     * @return Une liste d'utilisateurs correspondant aux critères.
     */
    List<User> findByStructureIdAndRole(Long structureId, UserRole role);

    /**
     * Récupère un utilisateur avec sa structure chargée (fetch join) pour éviter LazyInitializationException.
     * Utilisé après la transformation d'un utilisateur pour générer le JWT avec toutes les données.
     *
     * @param userId ID de l'utilisateur
     * @return Optional contenant l'utilisateur avec sa structure chargée
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.structure WHERE u.id = :userId")
    Optional<User> findUserWithStructureById(@Param("userId") Long userId);

    /**
     * Met à jour le type d'utilisateur (discriminateur), le rôle et la structure via requête native.
     * Cette méthode est utilisée lors de la création d'une structure pour promouvoir le créateur.
     *
     * @param userId      ID de l'utilisateur à mettre à jour
     * @param structureId ID de la structure à associer
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET " +
            "user_type = 'STRUCTURE_ADMINISTRATOR', " +
            "role = 'STRUCTURE_ADMINISTRATOR', " +
            "structure_id = :structureId " +
            "WHERE id = :userId", nativeQuery = true)
    void upgradeUserToStructureAdmin(@Param("userId") Long userId, @Param("structureId") Long structureId);

    @Modifying
    @Query(value = "UPDATE users SET user_type = :userType, role = :role, structure_id = :structureId WHERE id = :userId", nativeQuery = true)
    int updateUserTypeAndStructure(@Param("userId") Long userId,
                                   @Param("userType") String userType,
                                   @Param("role") String role,
                                   @Param("structureId") Long structureId);

    /**
     * Met à jour le type d'utilisateur (discriminateur) et le rôle d'un utilisateur existant.
     * Cette méthode est utilisée lors de la modification du rôle d'un membre d'équipe.
     *
     * @param userId   ID de l'utilisateur à mettre à jour
     * @param userType Nouveau type d'utilisateur (discriminateur)
     * @param role     Nouveau rôle
     * @return Nombre de lignes mises à jour
     */
    @Modifying
    @Query(value = "UPDATE users SET user_type = :userType, role = :role WHERE id = :userId", nativeQuery = true)
    int updateUserTypeAndRole(@Param("userId") Long userId,
                              @Param("userType") String userType,
                              @Param("role") String role);


    /**
     * Convertit un utilisateur Staff en Spectator en supprimant l'association à la structure.
     * Cette méthode est utilisée lors de la suppression d'un membre d'équipe.
     *
     * @param userId ID de l'utilisateur à convertir
     * @return Nombre de lignes mises à jour
     */
    @Modifying
    @Query(value = "UPDATE users SET user_type = 'SPECTATOR', role = 'SPECTATOR', structure_id = NULL WHERE id = :userId", nativeQuery = true)
    int convertUserToSpectator(@Param("userId") Long userId);

    /**
     * Convertit tous les utilisateurs Staff d'une structure en Spectator en une seule requête.
     * Utilisé lors de la dissolution d'une équipe complète.
     *
     * @param structureId ID de la structure
     * @return Nombre de lignes mises à jour
     */
    @Modifying
    @Query(value = "UPDATE users SET user_type = 'SPECTATOR', role = 'SPECTATOR', structure_id = NULL WHERE structure_id = :structureId", nativeQuery = true)
    int convertAllStructureUsersToSpectator(@Param("structureId") Long structureId);

    boolean findByStructureId(Long structureId);
}