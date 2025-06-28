package edu.cda.project.ticklybackend.dtos.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant un résumé d'un espace (Area) utilisé dans un événement.
 * Utilisé dans EventDetailResponseDto pour lister les espaces physiques concernés.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAreaSummaryDto {

    /**
     * L'ID unique de l'espace physique (StructureArea).
     */
    private Long id;

    /**
     * Le nom de l'espace physique (e.g., "Grande Scène Park").
     */
    private String name;
}
