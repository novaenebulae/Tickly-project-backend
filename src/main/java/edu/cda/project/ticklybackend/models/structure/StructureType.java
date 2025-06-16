package edu.cda.project.ticklybackend.models.structure;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité représentant un type de structure (ex: Salle de concert, Théâtre).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "structure_types")
public class StructureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du type de structure ne peut pas être vide.")
    @Size(max = 100, message = "Le nom du type de structure ne peut pas dépasser 100 caractères.")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 255, message = "L'icône ne peut pas dépasser 255 caractères.")
    @Column(length = 255)
    private String icon; // Nom ou chemin d'une icône associée (optionnel)
}