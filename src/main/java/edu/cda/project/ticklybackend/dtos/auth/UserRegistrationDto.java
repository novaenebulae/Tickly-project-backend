package edu.cda.project.ticklybackend.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Le prénom ne peut pas être vide.")
    @Size(min = 2, message = "Le prénom doit contenir au moins 2 caractères.")
    private String firstName;

    @NotBlank(message = "Le nom ne peut pas être vide.")
    @Size(min = 2, message = "Le nom doit contenir au moins 2 caractères.")
    private String lastName;

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    // Ajouter d'autres contraintes de complexité si nécessaire (ex: @Pattern)
    private String password;

    // Pas de champ confirmPassword ici, la validation se fait souvent côté client
    // ou via une validation personnalisée côté serveur si nécessaire.
    // Pour simplifier, nous l'omettons ici, en supposant que le client s'en charge.

    // Booléen pour différencier l'inscription d'un spectateur de celle d'un admin de structure.
    @NotNull(message = "Le champ 'createStructure' est requis.")
    private Boolean createStructure; // true si l'utilisateur veut créer une structure (devient admin)
}