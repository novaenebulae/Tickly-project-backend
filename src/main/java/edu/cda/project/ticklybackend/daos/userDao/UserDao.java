package edu.cda.project.ticklybackend.daos.userDao;

import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {

    User findUserById(Integer id);
}
