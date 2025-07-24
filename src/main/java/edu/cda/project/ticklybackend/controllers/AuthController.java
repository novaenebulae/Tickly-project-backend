package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.auth.*;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth") // Base path for this controller
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user and log them in",
            description = "Creates a new user account and returns a JWT token if registration is successful.")
    @ApiResponse(responseCode = "201", description = "User successfully registered and logged in",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid registration data")
    @ApiResponse(responseCode = "409", description = "Email is already in use")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerAndLoginUser(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("Registration request received for email: {}", registrationDto.getEmail());
        AuthResponseDto authResponse = authService.registerUser(registrationDto);
        log.info("Registration successful for user: {}", registrationDto.getEmail());
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Log in an existing user",
            description = "Validates user credentials and returns a JWT token upon successful authentication.")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid login data")
    @ApiResponse(responseCode = "401", description = "Incorrect credentials")
    @ApiResponse(responseCode = "403", description = "Email not validated")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(
            @Valid @RequestBody UserLoginDto loginDto) {
        log.info("Login attempt for email: {}", loginDto.getEmail());
        AuthResponseDto authResponse = authService.login(loginDto);
        log.info("Login successful for user: {}", loginDto.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Validate a user's email", description = "Validates the email using the received token. Upon success, returns a JWT token for automatic login.")
    @GetMapping("/validate-email")
    public ResponseEntity<AuthResponseDto> validateEmail(@RequestParam("token") String token) {
        log.info("Email validation request with token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        AuthResponseDto authResponse = authService.validateEmail(token);
        log.info("Email validation successful for user: {}", authResponse.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Request password reset", description = "Sends an email with a reset link if the email address is associated with an account.")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        log.info("Password reset request for email: {}", requestDto.getEmail());
        authService.forgotPassword(requestDto.getEmail());
        log.info("Reset email sent (if account exists) for: {}", requestDto.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password", description = "Updates the user's password using the provided token and new password.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        log.info("Password reset request with token: {}", resetDto.getToken().substring(0, Math.min(resetDto.getToken().length(), 10)) + "...");
        authService.resetPassword(resetDto);
        log.info("Password reset successful");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Refresh JWT token", 
            description = "Generates a new pair of access and refresh tokens using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        log.info("Token refresh request received");
        AuthResponseDto authResponse = authService.refreshToken(requestDto.getRefreshToken());
        log.info("Tokens refreshed successfully");
        return ResponseEntity.ok(authResponse);
    }
    
    @Operation(summary = "Logout user", 
            description = "Revokes the provided refresh token, effectively logging the user out.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        log.info("Logout request received");
        authService.logout(requestDto.getRefreshToken());
        log.info("Logout successful");
        return ResponseEntity.ok().build();
    }
}
