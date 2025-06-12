package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_favorite_structures", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "structure_id"}) // Un utilisateur ne peut favoriser une structure qu'une fois
})
public class UserFavoriteStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private Structure structure; // L'entité Structure est définie

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant addedAt;
}