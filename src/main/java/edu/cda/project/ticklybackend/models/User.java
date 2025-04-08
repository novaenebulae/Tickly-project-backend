package edu.cda.project.ticklybackend.models;

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
    protected Integer id;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    protected String mail;

    @NotBlank
    @Column(nullable = false)
    protected String password;

    protected boolean admin;

}
