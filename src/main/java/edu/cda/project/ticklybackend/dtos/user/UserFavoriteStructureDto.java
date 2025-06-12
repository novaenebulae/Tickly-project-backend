package edu.cda.project.ticklybackend.dtos.user;

import edu.cda.project.ticklybackend.dtos.structure.StructureSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteStructureDto {
    private Long id; // ID de l'enregistrement UserFavoriteStructure
    private Long userId;
    private StructureSummaryDto structure; // Détails de la structure favorisée
    private Instant addedAt;
}