package edu.cda.project.ticklybackend.daos.userDaos;

import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {

    User findUserById(Integer id);
}
