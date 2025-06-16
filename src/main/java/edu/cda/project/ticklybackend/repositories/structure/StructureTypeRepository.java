package edu.cda.project.ticklybackend.repositories.structure;

import edu.cda.project.ticklybackend.models.structure.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface StructureTypeRepository extends JpaRepository<StructureType, Long> {
    Set<StructureType> findByIdIn(List<Long> ids);
}