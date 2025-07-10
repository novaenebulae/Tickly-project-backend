package edu.cda.project.ticklybackend.controllers; // Adaptez si vous avez un sous-package 'user'

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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Utilisateurs", description = "Endpoints pour gérer les profils utilisateurs et les favoris.")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Récupérer le profil de l'utilisateur authentifié")
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
            LoggingUtils.logException(log, "Erreur lors de la récupération du profil utilisateur", e);
            throw e;
        }
    }

    @Operation(summary = "Mettre à jour le profil de l'utilisateur authentifié")
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
            LoggingUtils.logException(log, "Erreur lors de la mise à jour du profil utilisateur", e);
            throw e;
        }
    }

    @Operation(summary = "Uploader ou mettre à jour l'avatar de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FileUploadResponseDto.class)))
    @PostMapping(value = "/me/avatar", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponseDto> uploadMyAvatar(@RequestPart("file") MultipartFile file) {
        LoggingUtils.logMethodEntry(log, "uploadMyAvatar", "fileName", file.getOriginalFilename());
        try {
            String fileUrl = userService.updateCurrentUserAvatar(file);
            FileUploadResponseDto response = new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Avatar mis à jour avec succès.");
            LoggingUtils.logMethodExit(log, "uploadMyAvatar", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'upload de l'avatar utilisateur", e);
            throw e;
        }
    }

//    @Operation(summary = "Récupérer le profil d'un utilisateur par ID (Admin)",
//            description = "Nécessite le rôle SYSTEM_ADMINISTRATOR.")
//    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class)))
//    @GetMapping("/{userId}")
//    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
//    public ResponseEntity<UserProfileResponseDto> getUserProfileById(@PathVariable Long userId) {
//        return ResponseEntity.ok(userService.getUserProfile(userId));
//    }
//
//    @Operation(summary = "Rechercher des utilisateurs")
//    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Page.class)))
//    // Page<UserSearchResponseDto>
//    @GetMapping("/search")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<Page<UserSearchResponseDto>> searchUsers(
//            @RequestParam(required = false) String query,
//            @ParameterObject Pageable pageable) {
//        return ResponseEntity.ok(userService.searchUsers(query, pageable));
//    }

    @Operation(summary = "Lister les structures favorites de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = List.class)))
    // List<UserFavoriteStructureDto>
    @GetMapping("/me/favorites/structures")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserFavoriteStructureDto>> getMyFavoriteStructures() {
        LoggingUtils.logMethodEntry(log, "getMyFavoriteStructures");
        try {
            List<UserFavoriteStructureDto> favorites = userService.getCurrentUserFavoriteStructures();
            LoggingUtils.logMethodExit(log, "getMyFavoriteStructures", favorites);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des structures favorites", e);
            throw e;
        }
    }

    @Operation(summary = "Ajouter une structure aux favoris de l'utilisateur authentifié")
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
            LoggingUtils.logException(log, "Erreur lors de l'ajout de la structure ID " + requestDto.getStructureId() + " aux favoris", e);
            throw e;
        }
    }

    @Operation(summary = "Retirer une structure des favoris de l'utilisateur authentifié")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/me/favorites/structures/{structureId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMyFavoriteStructure(
            @Parameter(description = "ID de la structure à retirer des favoris") @PathVariable Long structureId) {
        LoggingUtils.logMethodEntry(log, "removeMyFavoriteStructure", "structureId", structureId);
        try {
            userService.removeCurrentUserFavoriteStructure(structureId);
            LoggingUtils.logMethodExit(log, "removeMyFavoriteStructure");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la suppression de la structure ID " + structureId + " des favoris", e);
            throw e;
        }
    }

    @DeleteMapping("/me")
    @Operation(summary = "Demander la suppression de mon compte", description = "Lance le processus de suppression de compte en envoyant un e-mail de confirmation. La suppression n'est pas immédiate.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestAccountDeletion() {
        LoggingUtils.logMethodEntry(log, "requestAccountDeletion");
        try {
            userService.requestAccountDeletion();
            LoggingUtils.logMethodExit(log, "requestAccountDeletion");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la demande de suppression de compte", e);
            throw e;
        }
    }

    @DeleteMapping("/confirm-deletion")
    @Operation(summary = "Confirmer la suppression du compte", description = "Supprime définitivement le compte et les données associées en utilisant le token reçu par e-mail. Cette action est irréversible.")
    public ResponseEntity<Void> confirmAccountDeletion(@RequestParam("token") String token) {
        LoggingUtils.logMethodEntry(log, "confirmAccountDeletion", "token", token);
        try {
            userService.confirmAccountDeletion(token);
            LoggingUtils.logMethodExit(log, "confirmAccountDeletion");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la confirmation de suppression de compte", e);
            throw e;
        }
    }
}
