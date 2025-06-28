package edu.cda.project.ticklybackend.models.structure;

import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité principale représentant une structure événementielle (lieu).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "structures")
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la structure ne peut pas être vide.")
    @Size(max = 255, message = "Le nom de la structure ne peut pas dépasser 255 caractères.")
    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "structure_has_types",
            joinColumns = @JoinColumn(name = "structure_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private Set<StructureType> types = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    private StructureAddress address;

    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StructureArea> areas = new ArrayList<>();

    @Size(max = 30, message = "Le numéro de téléphone ne peut pas dépasser 30 caractères.")
    private String phone;

    @Email(message = "Le format de l'email est invalide.")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères.")
    private String email;

    @Size(max = 2048, message = "L'URL du site web ne peut pas dépasser 2048 caractères.")
    @Column(length = 2048)
    private String websiteUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "structure_social_media_links", joinColumns = @JoinColumn(name = "structure_id"))
    @Column(name = "link", length = 2048)
    private List<String> socialMediaLinks = new ArrayList<>();

    @Column(length = 512)
    private String logoPath; // Chemin relatif ou nom du fichier du logo

    @Column(length = 512)
    private String coverPath; // Chemin relatif ou nom du fichier de l'image de couverture

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "structure_gallery_images", joinColumns = @JoinColumn(name = "structure_id"))
    @Column(name = "image_path", length = 512)
    private List<String> galleryImagePaths = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private User administrator;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}