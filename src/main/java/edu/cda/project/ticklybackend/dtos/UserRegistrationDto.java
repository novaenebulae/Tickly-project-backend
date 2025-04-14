package edu.cda.project.ticklybackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role; // Pour indiquer quel type d'utilisateur créer
    private Integer structureId;
}

