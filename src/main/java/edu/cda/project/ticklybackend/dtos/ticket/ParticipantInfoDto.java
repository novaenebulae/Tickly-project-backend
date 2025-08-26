package edu.cda.project.ticklybackend.dtos.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Ticket holder information used during reservation and for display.
 */
@Schema(description = "Ticket holder information.")
public class ParticipantInfoDto {

    @NotBlank(message = "Le prénom est requis.")
    @Size(min = 1, max = 255)
    @Schema(description = "Participant first name.", example = "John")
    private String firstName;

    @NotBlank(message = "Le nom de famille est requis.")
    @Size(min = 1, max = 255)
    @Schema(description = "Participant last name.", example = "Doe")
    private String lastName;

    @Email(message = "L'email doit être valide.")
    @Schema(description = "Participant email.", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Whether the ticket should be sent by email.", example = "false")
    private Boolean sendTicketByEmail = false;

}
