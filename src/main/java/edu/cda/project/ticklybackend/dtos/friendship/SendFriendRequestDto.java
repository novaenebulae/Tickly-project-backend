package edu.cda.project.ticklybackend.dtos.friendship;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO pour envoyer une demande d'ami via l'email de l'utilisateur.")
public class SendFriendRequestDto {
    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    @Schema(description = "Email de l'utilisateur à qui envoyer la demande.", requiredMode = Schema.RequiredMode.REQUIRED, example = "ami@example.com")
    private String email;
}
