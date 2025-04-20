package edu.cda.project.ticklybackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
// Retirer import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UserRegistrationDto {

    @NotBlank(message = "Le prénom est obligatoire") // OK pour String
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire") // OK pour String
    private String lastName;

    @NotBlank(message = "L'email est obligatoire") // OK pour String
    @Email(message = "Format de l'email invalide") // OK pour String
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire") // OK pour String
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") // Utiliser @Size
    private String password;

    // @NotBlank // <<<=== SUPPRIMER CETTE LIGNE
    private boolean createStructure = false; // Garder la valeur par défaut est une bonne idée
}
