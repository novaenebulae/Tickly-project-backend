package edu.cda.project.ticklybackend.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO résumé avec les informations publiques de base d'un utilisateur.
 * Utilisé pour représenter des amis, des membres d'équipe, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO résumé d'un utilisateur.")
public class UserSummaryDto {
    @Schema(description = "ID de l'utilisateur.", example = "10")
    private Long id;

    @Schema(description = "Prénom de l'utilisateur.", example = "Julien")
    private String firstName;

    @Schema(description = "Nom de l'utilisateur.", example = "Garcia")
    private String lastName;

    @Schema(description = "URL complète de l'avatar de l'utilisateur.", example = "http://localhost/static/avatars/avatar_10.png")
    private String avatarUrl;
}