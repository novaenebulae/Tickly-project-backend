package edu.cda.project.ticklybackend.daos.structureDao;

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructureDao extends JpaRepository<Structure, Integer> {

    public Structure findByName(String name);

    Structure findStructureById(int id);

    List<Structure> findByTypesId(Integer typeId);

}
