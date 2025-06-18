package edu.cda.project.ticklybackend.dtos.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Informations sur le détenteur du billet.")
public class
ParticipantInfoDto {

    @NotBlank(message = "Le prénom est requis.")
    @Size(min = 1, max = 255)
    @Schema(description = "Prénom du participant.", example = "Jean")
    private String firstName;

    @NotBlank(message = "Le nom de famille est requis.")
    @Size(min = 1, max = 255)
    @Schema(description = "Nom de famille du participant.", example = "Dupont")
    private String lastName;

    @NotBlank(message = "L'email est requis.")
    @Email(message = "L'email doit être valide.")
    @Schema(description = "Email du participant.", example = "jean.dupont@example.com")
    private String email;
}
