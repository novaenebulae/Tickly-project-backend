package edu.cda.project.ticklybackend.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDto {

    @Size(min = 2, message = "Le prénom doit contenir au moins 2 caractères.")
    private String firstName;

    @Size(min = 2, message = "Le nom doit contenir au moins 2 caractères.")
    private String lastName;

    @Email(message = "Le format de l'email est invalide.")
    private String email; // La logique de vérification d'unicité sera dans le service

}