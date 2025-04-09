package edu.cda.project.ticklybackend.daos.userDaos;

import edu.cda.project.ticklybackend.models.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<Role, Integer> {
}
