package edu.cda.project.ticklybackend.models.event;

import com.fasterxml.jackson.annotation.JsonBackReference;
import edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "event_audience_zone")
public class EventAudienceZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Link to the template this zone is based on.
     * This is a mandatory, non-nullable relationship.
     * FetchType.LAZY is used for performance, so the template is only loaded when accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private AudienceZoneTemplate template;


    // CHAMP RENOMMÉ ET CONSERVÉ
    /**
     * The capacity allocated specifically for this event.
     * This can be less than or equal to the template's maxCapacity.
     */
    @Column(nullable = false)
    private int allocatedCapacity;


    // RELATION EXISTANTE
    /**
     * The event this zone configuration belongs to.
     * JsonBackReference prevents serialization loops when fetching event data.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference
    private Event event;


    // CHAMPS SUPPRIMÉS (name, isActive, seatingType) car ils seront lus depuis 'template'


    @Override
    public String toString() {
        return "EventAudienceZone{" +
                "id=" + id +
                ", templateId=" + (template != null ? template.getId() : "null") +
                ", allocatedCapacity=" + allocatedCapacity +
                ", eventId=" + (event != null ? event.getId() : "null") +
                '}';
    }
}
