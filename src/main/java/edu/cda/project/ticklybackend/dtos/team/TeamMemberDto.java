package edu.cda.project.ticklybackend.dtos.team;

import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "DTO représentant un membre d'une équipe.")
public class TeamMemberDto {
    @Schema(description = "ID de l'enregistrement de l'adhésion.")
    private Long id;

    @Schema(description = "ID de l'utilisateur (si le compte est lié).")
    private Long userId;

    @Schema(description = "Prénom de l'utilisateur.")
    private String firstName;

    @Schema(description = "Nom de l'utilisateur.")
    private String lastName;

    @Schema(description = "Email du membre.")
    private String email;

    @Schema(description = "URL de l'avatar de l'utilisateur.")
    private String avatarUrl;

    @Schema(description = "Rôle du membre dans l'équipe.")
    private UserRole role;

    @Schema(description = "Statut de l'adhésion (ex: PENDING_INVITATION, ACTIVE).")
    private TeamMemberStatus status;

    @Schema(description = "Date à laquelle le membre a rejoint l'équipe.")
    private Instant joinedAt;
}