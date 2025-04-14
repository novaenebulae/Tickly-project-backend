package edu.cda.project.ticklybackend.daos.userDao;

import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    User findUserById(Integer id);
    Optional<User> findByEmail(String email);
    List<User> findByStructureId(Integer structureId);
    User findByIdAndStructureId(Integer id, Integer structureId);
}
