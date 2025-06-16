package edu.cda.project.ticklybackend.models.structure;

import edu.cda.project.ticklybackend.enums.SeatingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité modélisant une configuration type de zone d'audience
 * qui peut être associée à une StructureArea.
 * (ex: Fosse Debout, Balcon Rangée A)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audience_zone_templates")
public class AudienceZoneTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du modèle de zone ne peut pas être vide.")
    @Size(max = 255, message = "Le nom du modèle de zone ne peut pas dépasser 255 caractères.")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "La capacité maximale ne peut pas être nulle.")
    @Min(value = 0, message = "La capacité maximale doit être un nombre positif ou zéro.")
    @Column(nullable = false)
    private Integer maxCapacity;

    @NotNull(message = "Le type de placement ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingType seatingType;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private StructureArea area;
}