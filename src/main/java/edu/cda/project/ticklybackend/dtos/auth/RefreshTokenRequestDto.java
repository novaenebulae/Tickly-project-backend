package edu.cda.project.ticklybackend.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refresh token requests.
 * Used for both refresh token and logout endpoints.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDto {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}