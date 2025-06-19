package edu.cda.project.ticklybackend.models.team;

import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'équipe de gestion associée à une structure.
 * Pour l'instant, chaque structure n'a qu'une seule équipe, mais ce modèle permet une future extension.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false, unique = true)
    private Structure structure;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeamMember> members = new ArrayList<>();
}