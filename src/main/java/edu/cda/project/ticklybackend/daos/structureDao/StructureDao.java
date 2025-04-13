package edu.cda.project.ticklybackend.daos.structureDao;

import edu.cda.project.ticklybackend.models.structure.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructureDao extends JpaRepository<Structure, Integer> {
    List<Structure> findByTypesId(Integer typeId);
}
