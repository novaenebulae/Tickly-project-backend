package edu.cda.project.ticklybackend.daos.userDao;

import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffUserDao extends JpaRepository<StaffUser, Integer> {
    List<StaffUser> findByStructureId(Integer structureId);
    StaffUser findByIdAndStructureId(Integer id, Integer structureId);
}
