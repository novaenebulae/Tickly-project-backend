package edu.cda.project.ticklybackend.models.structure;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    protected String name;

//    @ManyToOne()
//    @JoinColumn(name = "structure_id", nullable = false)
//    private Structure structure;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Placement> placement = new ArrayList<>();

//    @OneToMany(mappedBy = "location")
//    @JsonIgnore
//    Set<EventLocation> eventSet;

}
