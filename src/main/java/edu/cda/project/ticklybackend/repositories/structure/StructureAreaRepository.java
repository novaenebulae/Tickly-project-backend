package edu.cda.project.ticklybackend.repositories.structure;

import edu.cda.project.ticklybackend.models.structure.StructureArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StructureAreaRepository extends JpaRepository<StructureArea, Long> {
    List<StructureArea> findByStructureId(Long structureId);

    Optional<StructureArea> findByIdAndStructureId(Long areaId, Long structureId);

    boolean existsByIdAndStructureId(Long areaId, Long structureId);
}