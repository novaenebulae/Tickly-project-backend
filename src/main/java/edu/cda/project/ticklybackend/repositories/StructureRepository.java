package edu.cda.project.ticklybackend.repositories;

import edu.cda.project.ticklybackend.models.structure.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {
}