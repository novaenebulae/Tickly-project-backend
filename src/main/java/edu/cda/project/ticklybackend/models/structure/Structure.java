package edu.cda.project.ticklybackend.models.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.views.DisplayStructure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    @JsonView(DisplayStructure.class)
    protected String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    protected String description;

    @ManyToMany
    @JoinTable(
            name="structure_structure_type",
            joinColumns = @JoinColumn(name="structure_id"),
            inverseJoinColumns = @JoinColumn(name ="type_id")
    )
    protected List<StructureType> types = new ArrayList<>();

    @OneToMany(mappedBy="structure", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    protected List<User> staff = new ArrayList<>();

    @OneToOne(mappedBy = "structure", cascade = CascadeType.ALL) // Référence le champ 'structure' dans Address
    @JsonManagedReference
    private Address address;

    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL) // Référence le champ 'structure' dans Address
    @JsonManagedReference
    private List<Location> locations = new ArrayList<>();

}
