package edu.cda.project.ticklybackend.dtos.team;

import edu.cda.project.ticklybackend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO pour mettre à jour le rôle d'un membre de l'équipe.")
public class UpdateMemberRoleDto {
    @NotNull
    @Schema(description = "Le nouveau rôle à assigner au membre.", example = "RESERVATION_SERVICE", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRole role;
}