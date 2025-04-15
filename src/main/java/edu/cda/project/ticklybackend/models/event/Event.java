package edu.cda.project.ticklybackend.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @NotNull
    @Column(name = "category", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @NotNull
    @Column(name = "status", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    @JsonIgnore
    private Structure structure;

    @OneToMany(mappedBy = "event")
    @JsonManagedReference("event-locations")
    Set<EventLocation> locationSet;

}
