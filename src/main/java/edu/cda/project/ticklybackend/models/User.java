package edu.cda.project.ticklybackend.models;

import com.fasterxml.jackson.annotation.JsonView;
import edu.cda.project.ticklybackend.views.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.User.class)
    protected Integer id;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    @JsonView(Views.User.class)
    protected String mail;

    @NotBlank
    @Column(nullable = false)
    protected String password;

    @JsonView(Views.User.class)
    protected boolean admin;

}
