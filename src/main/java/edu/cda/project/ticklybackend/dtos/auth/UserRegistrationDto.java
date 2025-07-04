package edu.cda.project.ticklybackend.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    private String password;


    @AssertTrue(message = "Vous devez accepter les termes pour vous inscrire.")
    private boolean termsAccepted;

    /**
     * Si un token d'invitation est fourni, le processus d'inscription
     * validera l'email et acceptera l'invitation en une seule étape.
     */
    @Schema(description = "Token d'invitation optionnel pour rejoindre une équipe lors de l'inscription.")
    private String invitationToken;
}
