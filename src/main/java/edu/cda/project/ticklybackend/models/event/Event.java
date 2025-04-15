package edu.cda.project.ticklybackend.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "event")
public class Event {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @NotBlank
    @Column(name = "endDate", nullable = false)
    private Date endDate;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @NotBlank
    @Column(name = "imageUrl", nullable = false)
    private String imageUrl;

    @NotBlank
    @Column(name = "category", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @NotBlank
    @Column(name = "status", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Structure structure;

    @OneToMany(mappedBy = "event")
    @JsonManagedReference("event-locations")
    Set<EventLocation> locationSet;

}
