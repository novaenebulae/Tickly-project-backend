package edu.cda.project.ticklybackend.models.event;

import edu.cda.project.ticklybackend.enums.SeatingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une zone d'audience spécifique configurée pour un événement particulier.
 * Cette entité permet de définir des capacités et des types de placement qui peuvent
 * différer des modèles par défaut de la structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_audience_zones")
public class EventAudienceZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingType seatingType;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * ID optionnel du modèle de zone de la structure (AudienceZoneTemplate)
     * sur lequel cette configuration est basée. Utile pour la traçabilité.
     */
    private Long baseAudienceZoneTemplateId;
}