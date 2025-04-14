package edu.cda.project.ticklybackend.models.structure;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.cda.project.ticklybackend.models.event.EventLocation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    protected String name;

    @ManyToOne()
    @JoinColumn(name = "structure_id", nullable = false)
    @JsonBackReference("structure")
    private Structure structure;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Placement> placement = new ArrayList<>();

    @OneToMany(mappedBy = "location")
    @JsonIgnore
    Set<EventLocation> eventSet;

}
