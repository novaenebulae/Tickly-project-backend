package edu.cda.project.ticklybackend.models.event;


import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY pour ne pas charger systématiquement
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory category;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String fullDescription;

    @ElementCollection(fetch = FetchType.LAZY) // Charger les tags seulement si nécessaire
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    // Pour l'instant, nous supposons que l'adresse de l'événement est celle de la structure.
    // Si un événement peut avoir une adresse différente, décommentez et adaptez AddressDto/Entity.
    // @Embedded
    // private Address eventAddress; // Ou une entité Address dédiée si plus complexe

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private Structure structure; // Structure organisatrice

    private boolean isFreeEvent = true;

    // Pour la photo principale de l'événement
    private String mainPhotoPath; // Chemin relatif ou nom du fichier

    // Pour une galerie de photos de l'événement
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "event_gallery_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_path")
    private List<String> eventPhotoPaths = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id") // Utilisateur qui a créé l'événement (StaffUser)
    private User creator;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // Champs pour la mise en avant (selon la documentation API)
    private boolean displayOnHomepage = false;
    private boolean isFeaturedEvent = false;
}