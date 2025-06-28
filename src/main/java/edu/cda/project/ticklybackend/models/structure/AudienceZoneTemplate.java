package edu.cda.project.ticklybackend.models.structure;

import com.fasterxml.jackson.annotation.JsonBackReference;
import edu.cda.project.ticklybackend.enums.SeatingType;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "audience_zone_template")
public class AudienceZoneTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int maxCapacity;

    @Column(nullable = false)
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SeatingType seatingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    @JsonBackReference // Prevents serialization loop
    private StructureArea area;

    // RELATION AJOUTÉE
    /**
     * Represents all the event-specific configurations that use this template.
     * This allows for tracking the usage of a template across different events.
     * The mappedBy attribute points to the 'template' field in the EventAudienceZone entity.
     * CascadeType.ALL means that if this template is deleted, all its associated event zones are also deleted.
     * OrphanRemoval=true ensures that if an EventAudienceZone is removed from this list, it is also deleted from the database.
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EventAudienceZone> eventAudienceZones;

    @Override
    public String toString() {
        return "AudienceZoneTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", maxCapacity=" + maxCapacity +
                ", isActive=" + isActive +
                ", seatingType=" + seatingType +
                ", areaId=" + (area != null ? area.getId() : "null") +
                '}';
    }
}
