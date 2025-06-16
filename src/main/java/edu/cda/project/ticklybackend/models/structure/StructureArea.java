package edu.cda.project.ticklybackend.models.structure;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un espace physique spécifique au sein d'une structure.
 * (ex: Salle A, Tribune Nord)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "structure_areas")
public class StructureArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'espace ne peut pas être vide.")
    @Size(max = 255, message = "Le nom de l'espace ne peut pas dépasser 255 caractères.")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La capacité maximale ne peut pas être nulle.")
    @Min(value = 0, message = "La capacité maximale doit être un nombre positif ou zéro.")
    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private Structure structure;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AudienceZoneTemplate> audienceZoneTemplates = new ArrayList<>();
}