package edu.cda.project.ticklybackend.repositories.structure;

import edu.cda.project.ticklybackend.models.structure.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long>, JpaSpecificationExecutor<Structure> {


}