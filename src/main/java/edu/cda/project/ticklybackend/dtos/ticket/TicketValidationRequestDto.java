package edu.cda.project.ticklybackend.dtos.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requête pour valider un billet en utilisant la valeur de son QR code.")
public class TicketValidationRequestDto {

    @NotBlank(message = "La valeur du QR code ne peut pas être vide.")
    @Schema(description = "La valeur unique scannée depuis le QR code du billet.", required = true)
    private String scannedQrCodeValue;
}
