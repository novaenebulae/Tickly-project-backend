package edu.cda.project.ticklybackend.dtos.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private Long structureId; // Si l'utilisateur est lié à une structure (ex: StaffUser)
    private String avatarUrl; // URL complète de l'avatar
    private Instant createdAt;
    private Instant updatedAt;
}