package edu.cda.project.ticklybackend.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.cda.project.ticklybackend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response payload containing tokens and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response payload containing tokens and user information.")
public class AuthResponseDto {

    @Schema(description = "JWT access token.")
    private String accessToken;

    @Schema(description = "Token type.", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token time-to-live in seconds.")
    private Long expiresIn;

    @Schema(description = "Refresh token to obtain a new access token.")
    private String refreshToken;

    @Schema(description = "Authenticated user identifier.")
    private Long userId;

    @Schema(description = "Authenticated user email.")
    private String email;

    @Schema(description = "Authenticated user first name.")
    private String firstName;

    @Schema(description = "Authenticated user last name.")
    private String lastName;

    @Schema(description = "Authenticated user role.")
    private UserRole role;

    @Schema(description = "Associated structure ID when applicable.")
    private Long structureId;

    @Schema(description = "URL of the user's avatar image.")
    private String avatarUrl;
}