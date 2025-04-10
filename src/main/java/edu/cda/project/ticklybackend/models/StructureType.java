package edu.cda.project.ticklybackend.models;


import com.fasterxml.jackson.annotation.JsonView;
import edu.cda.project.ticklybackend.views.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class StructureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column
    @NotBlank
    @JsonView(Views.Public.class)
    protected String type;
}
