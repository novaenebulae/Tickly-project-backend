package edu.cda.project.ticklybackend.daos.userDao;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    User findUserById(Integer id);

    User findUserByEmail(String email);

    @Modifying // Nécessaire pour les requêtes UPDATE/DELETE
    @Query("UPDATE User u SET u.role = :newRole WHERE u.id = :userId")
    int updateUserRoleColumn(@Param("userId") Integer userId, @Param("newRole") UserRole newRole);
}
