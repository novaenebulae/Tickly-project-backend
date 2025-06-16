package edu.cda.project.ticklybackend.repositories.structure;

import edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AudienceZoneTemplateRepository extends JpaRepository<AudienceZoneTemplate, Long> {
    List<AudienceZoneTemplate> findByAreaId(Long areaId);

    Optional<AudienceZoneTemplate> findByIdAndAreaId(Long templateId, Long areaId);

    List<AudienceZoneTemplate> findByAreaStructureId(Long structureId); // Pour récupérer toutes les zones d'une structure
}