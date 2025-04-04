package edu.cda.project.ticklybackend.dao;

import edu.cda.project.ticklybackend.models.Structure;
import edu.cda.project.ticklybackend.models.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureTypeDao extends JpaRepository<StructureType, Integer> {

    public StructureType findByType(String type);
}
