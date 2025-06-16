package edu.cda.project.ticklybackend.repositories.structure;

import edu.cda.project.ticklybackend.models.structure.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long>, JpaSpecificationExecutor<Structure> {
    // JpaSpecificationExecutor est ajouté pour permettre des requêtes dynamiques/filtrées plus complexes si besoin.

    // Exemple de méthode de recherche si nécessaire (non utilisé directement dans ce plan mais utile) :
    // Page<Structure> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Structure> findByIdAndAdministratorId(Long structureId, Long administratorId);
}