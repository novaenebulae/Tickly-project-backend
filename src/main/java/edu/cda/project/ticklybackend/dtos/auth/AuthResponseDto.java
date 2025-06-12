package edu.cda.project.ticklybackend.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // Pour une construction facile de l'objet
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut pas les champs nuls dans le JSON
public class AuthResponseDto {

    private String accessToken;
    private String tokenType = "Bearer"; // Toujours "Bearer"
    private Long expiresIn; // Durée de validité en millisecondes
    // private String refreshToken; // Optionnel, si implémenté

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Boolean needsStructureSetup; // Pour STRUCTURE_ADMINISTRATOR
    private Long structureId; // ID de la structure associée (pour StaffUser)
    private String avatarUrl; // URL complète de l'avatar
}