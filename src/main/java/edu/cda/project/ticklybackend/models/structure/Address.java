package edu.cda.project.ticklybackend.models.structure;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;
import edu.cda.project.ticklybackend.views.DisplayStructure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    @JsonView(DisplayStructure.class)
    protected String country;

    @Column(nullable = false)
    protected String city;

    @Column(nullable = false)
    protected String postal_code;

    @Column(nullable = false)
    protected String street;

    @Column
    protected String number;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "structure_id")
    @JsonBackReference
    private Structure structure;

}
