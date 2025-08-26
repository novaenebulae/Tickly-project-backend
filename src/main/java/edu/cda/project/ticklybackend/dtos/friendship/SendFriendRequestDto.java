package edu.cda.project.ticklybackend.dtos.friendship;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Payload to send a friend request using a user's email.")
public class SendFriendRequestDto {
    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    @Schema(description = "Email of the user to whom the request is sent.", requiredMode = Schema.RequiredMode.REQUIRED, example = "friend@example.com")
    private String email;
}
