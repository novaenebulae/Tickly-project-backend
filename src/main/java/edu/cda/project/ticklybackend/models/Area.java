package edu.cda.project.ticklybackend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    protected String name;

    @ManyToOne()
    @JoinColumn(name = "structure_id", nullable = false)
    @JsonBackReference
    private Structure structure;

}
