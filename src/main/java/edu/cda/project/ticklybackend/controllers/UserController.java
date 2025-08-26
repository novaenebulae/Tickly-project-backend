package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.user.FavoriteStructureRequestDto;
import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import edu.cda.project.ticklybackend.services.interfaces.UserService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * User endpoints for profile management, avatar upload, favorite structures,
 * and account deletion workflows.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Endpoints for managing user profiles and favorites.")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get the authenticated user's profile")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class)))
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponseDto> getMyProfile() {
        LoggingUtils.logMethodEntry(log, "getMyProfile");
        try {
            UserProfileResponseDto profile = userService.getCurrentUserProfile();
            LoggingUtils.logMethodExit(log, "getMyProfile", profile);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error retrieving user profile", e);
            throw e;
        }
    }

    @Operation(summary = "Update the authenticated user's profile")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class)))
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponseDto> updateMyProfile(@Valid @RequestBody UserProfileUpdateDto updateDto) {
        LoggingUtils.logMethodEntry(log, "updateMyProfile", "updateDto", updateDto);
        try {
            UserProfileResponseDto updatedProfile = userService.updateCurrentUserProfile(updateDto);
            LoggingUtils.logMethodExit(log, "updateMyProfile", updatedProfile);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error updating user profile", e);
            throw e;
        }
    }

    @Operation(summary = "Upload or update the authenticated user's avatar")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FileUploadResponseDto.class)))
    @PostMapping(value = "/me/avatar", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponseDto> uploadMyAvatar(@RequestPart("file") MultipartFile file) {
        LoggingUtils.logMethodEntry(log, "uploadMyAvatar", "fileName", file.getOriginalFilename());
        try {
            String fileUrl = userService.updateCurrentUserAvatar(file);
            FileUploadResponseDto response = new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Avatar updated successfully.");
            LoggingUtils.logMethodExit(log, "uploadMyAvatar", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error uploading user avatar", e);
            throw e;
        }
    }

    @Operation(summary = "List the authenticated user's favorite structures")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = List.class)))
    @GetMapping("/me/favorites/structures")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserFavoriteStructureDto>> getMyFavoriteStructures() {
        LoggingUtils.logMethodEntry(log, "getMyFavoriteStructures");
        try {
            List<UserFavoriteStructureDto> favorites = userService.getCurrentUserFavoriteStructures();
            LoggingUtils.logMethodExit(log, "getMyFavoriteStructures", favorites);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error retrieving favorite structures", e);
            throw e;
        }
    }

    @Operation(summary = "Add a structure to the authenticated user's favorites")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = UserFavoriteStructureDto.class)))
    @PostMapping("/me/favorites/structures")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserFavoriteStructureDto> addMyFavoriteStructure(
            @Valid @RequestBody FavoriteStructureRequestDto requestDto) {
        LoggingUtils.logMethodEntry(log, "addMyFavoriteStructure", "structureId", requestDto.getStructureId());
        try {
            UserFavoriteStructureDto favoriteDto = userService.addCurrentUserFavoriteStructure(requestDto.getStructureId());
            LoggingUtils.logMethodExit(log, "addMyFavoriteStructure", favoriteDto);
            return new ResponseEntity<>(favoriteDto, HttpStatus.CREATED);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error adding structure ID " + requestDto.getStructureId() + " to favorites", e);
            throw e;
        }
    }

    @Operation(summary = "Remove a structure from the authenticated user's favorites")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/me/favorites/structures/{structureId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMyFavoriteStructure(
            @Parameter(description = "ID of the structure to remove from favorites") @PathVariable Long structureId) {
        LoggingUtils.logMethodEntry(log, "removeMyFavoriteStructure", "structureId", structureId);
        try {
            userService.removeCurrentUserFavoriteStructure(structureId);
            LoggingUtils.logMethodExit(log, "removeMyFavoriteStructure");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error removing structure ID " + structureId + " from favorites", e);
            throw e;
        }
    }

    @DeleteMapping("/me")
    @Operation(summary = "Request account deletion", description = "Initiates the account deletion process by sending a confirmation email. Deletion is not immediate.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestAccountDeletion() {
        LoggingUtils.logMethodEntry(log, "requestAccountDeletion");
        try {
            userService.requestAccountDeletion();
            LoggingUtils.logMethodExit(log, "requestAccountDeletion");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error requesting account deletion", e);
            throw e;
        }
    }

    @DeleteMapping("/confirm-deletion")
    @Operation(summary = "Confirm account deletion", description = "Permanently deletes the account and associated data using the token received by email. This action is irreversible.")
    public ResponseEntity<Void> confirmAccountDeletion(@RequestParam("token") String token) {
        LoggingUtils.logMethodEntry(log, "confirmAccountDeletion", "token", token);
        try {
            userService.confirmAccountDeletion(token);
            LoggingUtils.logMethodExit(log, "confirmAccountDeletion");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Error confirming account deletion", e);
            throw e;
        }
    }
}
