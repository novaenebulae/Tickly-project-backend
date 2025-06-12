package edu.cda.project.ticklybackend.models.structure;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "structure_types")
public class StructureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column // Optionnel: pour une icône ou un slug
    private String icon;

    // Relation inverse si vous voulez naviguer de StructureType vers Structures
    // @ManyToMany(mappedBy = "types")
    // private Set<Structure> structures;
}