package edu.cda.project.ticklybackend.daos.userDao;

import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffUserDao extends JpaRepository<StaffUser, Integer> {
    List<StaffUser> findByStructureId(Integer structureId);

}
