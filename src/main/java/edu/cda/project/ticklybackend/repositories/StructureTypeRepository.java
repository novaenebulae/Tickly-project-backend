package edu.cda.project.ticklybackend.repositories;


import edu.cda.project.ticklybackend.models.structure.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureTypeRepository extends JpaRepository<StructureType, Long> {
}