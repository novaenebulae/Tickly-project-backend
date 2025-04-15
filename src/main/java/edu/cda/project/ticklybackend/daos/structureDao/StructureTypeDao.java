package edu.cda.project.ticklybackend.daos.structureDao;

import edu.cda.project.ticklybackend.models.structure.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureTypeDao extends JpaRepository<StructureType, Integer> {
    StructureType findStructureTypeById(Integer typeId);
}
