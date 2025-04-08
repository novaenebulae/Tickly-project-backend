package edu.cda.project.ticklybackend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Placement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column
    @NotBlank
    protected String name;

    @Column
    protected Integer price;

    @Column
    protected Integer capacity;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition =
            "ENUM('SEAT_PLACEMENT', 'FREE_PLACEMENT')")
    private PlacementType placementType;

    @ManyToOne()
    @JoinColumn(name = "location_id", nullable = false)
    @JsonBackReference
    private Location location;

}

