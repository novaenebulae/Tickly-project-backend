package edu.cda.project.ticklybackend.dtos.team;

import edu.cda.project.ticklybackend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO pour inviter un nouveau membre dans une équipe.")
public class InviteMemberRequestDto {
    @NotBlank
    @Email
    @Schema(description = "Email de la personne à inviter.", example = "nouveau.membre@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull
    @Schema(description = "Rôle à assigner au nouveau membre.", example = "ORGANIZATION_SERVICE", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRole role;
}