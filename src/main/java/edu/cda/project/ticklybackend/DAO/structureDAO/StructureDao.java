package edu.cda.project.ticklybackend.DAO.structureDAO;

import edu.cda.project.ticklybackend.models.structure.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureDao extends JpaRepository<Structure, Integer> {

    Structure findStructureById(int id);
}
