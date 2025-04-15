package edu.cda.project.ticklybackend.models.structure;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import edu.cda.project.ticklybackend.views.Views;
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
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @JsonView(Views.Public.class)
    @NotBlank
    protected String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @JsonView(Views.Public.class)
    protected String description;

    @ManyToMany
    @JoinTable(
            name = "structure_structure_type",
            joinColumns = @JoinColumn(name = "structure_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    @JsonView(Views.Public.class)
    protected List<StructureType> types = new ArrayList<>();

    @OneToOne(mappedBy = "structure", cascade = CascadeType.ALL) // Référence le champ 'structure' dans Address
    private Address address;

    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL) // Référence le champ 'structure' dans Address
    private List<Location> locations = new ArrayList<>();

    @OneToMany(mappedBy = "structure", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<StaffUser> staffUsers = new ArrayList<>();

    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

}
